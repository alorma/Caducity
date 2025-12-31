package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.ProductInstanceRoomEntity
import com.alorma.caducity.data.datasource.room.toModel
import com.alorma.caducity.data.datasource.room.toRoomEntity
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.ProductsListFilter
import com.alorma.caducity.time.clock.AppClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

class RoomProductDataSource(
  database: AppDatabase,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) : ProductDataSource {

  private val productDao = database.productDao()

  override fun getProducts(filter: ProductsListFilter): Flow<ImmutableList<ProductWithInstances>> {
    val daoFlow = when (filter) {
      is ProductsListFilter.All -> {
        productDao.getAllProductsWithInstances()
      }

      is ProductsListFilter.ByDate -> {
        val startOfDayMillis =
          filter.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val nextDayMillis = startOfDayMillis + 1.days.inWholeMilliseconds
        productDao.getProductsWithInstancesByDate(startOfDayMillis, nextDayMillis)
      }

      is ProductsListFilter.ByDateRange -> {
        val startMillis =
          filter.startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = filter.endDate.atStartOfDayIn(TimeZone.currentSystemDefault())
          .toEpochMilliseconds() + 1.days.inWholeMilliseconds
        productDao.getProductsWithInstancesByDateRange(startMillis, endMillis)
      }

      is ProductsListFilter.ByStatus -> {
        // Convert status filters to date range queries for SQL optimization
        when {
          filter.statuses.isEmpty() -> {
            productDao.getAllProductsWithInstances()
          }

          filter.statuses.size == 1 -> {
            // Single status: fully optimized SQL query
            val status = filter.statuses.first()
            val (minDate, maxDate) = statusToDateRange(status)
            productDao.getProductsWithInstancesByDateRange(minDate, maxDate)
          }

          else -> {
            // Multiple statuses: hybrid approach - SQL narrows range, then in-memory filter
            val dateRanges = filter.statuses.map { statusToDateRange(it) }
            val minDate = dateRanges.minOf { it.first }
            val maxDate = dateRanges.maxOf { it.second }
            productDao.getProductsWithInstancesByDateRange(minDate, maxDate)
          }
        }
      }
    }

    return daoFlow.map { roomEntities ->
      val products = roomEntities.map { it.toModel(appClock, expirationThresholds) }

      // Apply in-memory status filter only for multiple statuses
      // (to handle non-contiguous date ranges like Expired + Fresh)
      val filtered = if (filter is ProductsListFilter.ByStatus && filter.statuses.size > 1) {
        products.filter { productWithInstances ->
          // Keep product if it has at least one instance with the requested status
          productWithInstances.instances.any { instance ->
            instance.status in filter.statuses
          }
        }
      } else {
        products
      }

      filtered.toImmutableList()
    }
  }

  private fun statusToDateRange(status: InstanceStatus): Pair<Long, Long> {
    val now = appClock.now()
    val nowMillis = now.toEpochMilliseconds()
    val expiringSoonMillis =
      now.plus(expirationThresholds.soonExpiringThreshold).toEpochMilliseconds()

    return when (status) {
      InstanceStatus.Expired -> {
        Pair(0L, nowMillis) // From epoch to now
      }

      InstanceStatus.ExpiringSoon -> {
        Pair(nowMillis, expiringSoonMillis) // From now to (now + threshold)
      }

      InstanceStatus.Fresh -> {
        Pair(expiringSoonMillis, Long.MAX_VALUE) // From (now + threshold) to infinity
      }
    }
  }

  override fun getProduct(productId: String): Flow<Result<ProductWithInstances>> {
    return productDao.getProductWithInstances(productId)
      .map { roomEntity ->
        roomEntity?.let { Result.success(it.toModel(appClock, expirationThresholds)) }
          ?: Result.failure(NoSuchElementException("Product with id $productId not found"))
      }
  }

  override suspend fun createProduct(
    product: Product,
    instances: ImmutableList<ProductInstance>,
  ) {
    productDao.insertProduct(product.toRoomEntity())
    instances.forEach { instance ->
      productDao.insertProductInstance(instance.toRoomEntity(product.id))
    }
  }

  override suspend fun addInstance(productId: String, instance: ProductInstance) {
    productDao.insertProductInstance(instance.toRoomEntity(productId))
  }

  override suspend fun deleteInstance(instanceId: String) {
    productDao.deleteProductInstance(instanceId)
  }

  override suspend fun markInstanceAsConsumed(instanceId: String) {
    productDao.getProductInstance(instanceId)?.let { instance ->
      val updatedInstance = instance.copy(
        consumedDate = appClock.now().toEpochMilliseconds(),
        pausedDate = null, // Clear frozen state if it was frozen
        remainingDays = null
      )
      productDao.updateProductInstance(updatedInstance)
    }
  }

  override suspend fun freezeInstance(instanceId: String, remainingDays: Int) {
    productDao.getProductInstance(instanceId)?.let { instance ->
      val updatedInstance = instance.copy(
        pausedDate = appClock.now().toEpochMilliseconds(),
        remainingDays = remainingDays
      )
      productDao.updateProductInstance(updatedInstance)
    }
  }

  override suspend fun unfreezeInstance(instanceId: String) {
    productDao.getProductInstance(instanceId)?.let { instance ->
      val pausedDate = instance.pausedDate
      val remainingDays = instance.remainingDays

      if (pausedDate != null && remainingDays != null) {
        // Calculate new expiration date: now + remaining days
        val now = appClock.now()
        val newExpirationDate = now.toEpochMilliseconds() + (remainingDays.days.inWholeMilliseconds)

        val updatedInstance = instance.copy(
          expirationDate = newExpirationDate,
          pausedDate = null,
          remainingDays = null
        )
        productDao.updateProductInstance(updatedInstance)
      }
    }
  }
}