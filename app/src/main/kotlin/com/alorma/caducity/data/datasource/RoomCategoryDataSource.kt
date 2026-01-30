package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.toModel
import com.alorma.caducity.data.datasource.room.toRoomEntity
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.NewProductInstance
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.ProductsListFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import java.util.UUID
import kotlin.time.Duration.Companion.days

class RoomCategoryDataSource(
  database: AppDatabase,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) : ProductDataSource {

  private val categoryDao = database.categoryDao()
  private val itemDao = database.itemDao()

  override fun getProducts(filter: ProductsListFilter): Flow<ImmutableList<ProductWithInstances>> {
    val daoFlow = when (filter) {
      is ProductsListFilter.All -> {
        categoryDao.getAllCategoriesWithItems()
      }

      is ProductsListFilter.ByDate -> {
        val startOfDayMillis =
          filter.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val nextDayMillis = startOfDayMillis + 1.days.inWholeMilliseconds
        categoryDao.getCategoriesWithItemsByDate(startOfDayMillis, nextDayMillis)
      }

      is ProductsListFilter.ByDateRange -> {
        val startMillis =
          filter.startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = filter.endDate.atStartOfDayIn(TimeZone.currentSystemDefault())
          .toEpochMilliseconds() + 1.days.inWholeMilliseconds
        categoryDao.getCategoriesWithItemsByDateRange(startMillis, endMillis)
      }

      is ProductsListFilter.ByStatus -> {
        // Convert status filters to date range queries for SQL optimization
        when {
          filter.statuses.isEmpty() -> {
            categoryDao.getAllCategoriesWithItems()
          }

          filter.statuses.size == 1 -> {
            // Single status: fully optimized SQL query
            val status = filter.statuses.first()
            val (minDate, maxDate) = statusToDateRange(status)
            categoryDao.getCategoriesWithItemsByDateRange(minDate, maxDate)
          }

          else -> {
            // Multiple statuses: hybrid approach - SQL narrows range, then in-memory filter
            val dateRanges = filter.statuses.map { statusToDateRange(it) }
            val minDate = dateRanges.minOf { it.first }
            val maxDate = dateRanges.maxOf { it.second }
            categoryDao.getCategoriesWithItemsByDateRange(minDate, maxDate)
          }
        }
      }
    }

    return daoFlow.map { roomEntities ->
      // Filter consumed instances at the source
      val filteredEntities = roomEntities.map { it.filterConsumed() }
      val products = filteredEntities.map { it.toModel(appClock, expirationThresholds) }

      // Apply in-memory status filter only for multiple statuses
      // (to handle non-contiguous date ranges like Expired + Fresh)
      val filtered = if (filter is ProductsListFilter.ByStatus && filter.statuses.size > 1) {
        products.filter { productWithInstances ->
          // Keep product if it has at least one instance with the requested status
          val allInstances = productWithInstances.variants.flatMap { it.instances } +
              productWithInstances.standaloneInstances
          allInstances.any { instance ->
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
    val expiringSoonMillis =
      now.plus(expirationThresholds.soonExpiringThreshold).toEpochMilliseconds()

    // Calculate start of today for consistent date boundary handling
    val todayStartMillis = now
      .date()
      .atStartOfDayIn(TimeZone.currentSystemDefault())
      .toEpochMilliseconds()

    return when (status) {
      InstanceStatus.Expired -> {
        // From epoch to yesterday (before today starts) - excludes items expiring today
        Pair(0L, todayStartMillis)
      }

      InstanceStatus.ExpiringSoon -> {
        // From today (start of day) to (now + threshold) - includes items expiring today
        Pair(todayStartMillis, expiringSoonMillis)
      }

      InstanceStatus.Fresh -> {
        Pair(expiringSoonMillis, Long.MAX_VALUE) // From (now + threshold) to infinity
      }

      InstanceStatus.Frozen -> {
        // Frozen items don't have a date range filter - return all dates
        Pair(0L, Long.MAX_VALUE)
      }
    }
  }

  override fun getProduct(productId: String): Flow<Result<ProductWithInstances>> {
    return categoryDao.getCategoryWithItems(productId)
      .map { roomEntity ->
        roomEntity?.let {
          // Filter consumed instances before converting
          Result.success(it.filterConsumed().toModel(appClock, expirationThresholds))
        } ?: Result.failure(NoSuchElementException("Product with id $productId not found"))
      }
  }

  override suspend fun createProduct(
    product: Product,
    instances: ImmutableList<ProductInstance>,
  ) {
    categoryDao.insertCategory(product.toRoomEntity())
    instances.forEach { instance ->
      itemDao.insertItem(instance.toRoomEntity(product.id))
    }
  }

  override suspend fun addInstance(
    productId: String,
    instance: NewProductInstance
  ): String {
    val id = UUID.randomUUID().toString()

    itemDao.insertItem(
      instance.toRoomEntity(id = id, categoryId = productId),
    )
    return id
  }

  override suspend fun deleteInstance(instanceId: String) {
    itemDao.deleteItem(instanceId)
  }

  override suspend fun getInstance(instanceId: String): ProductInstance? {
    return itemDao.getItem(instanceId)?.toModel(appClock, expirationThresholds)
  }

  override suspend fun markInstanceAsConsumed(instanceId: String) {
    itemDao.getItem(instanceId)?.let { item ->
      val updatedItem = item.copy(
        consumedDate = appClock.now().toEpochMilliseconds(),
        pausedDate = null, // Clear frozen state if it was frozen
        remainingDays = null
      )
      itemDao.updateItem(updatedItem)
    }
  }

  override suspend fun freezeInstance(instanceId: String, remainingDays: Int) {
    itemDao.getItem(instanceId)?.let { item ->
      val updatedItem = item.copy(
        pausedDate = appClock.now().toEpochMilliseconds(),
        remainingDays = remainingDays
      )
      itemDao.updateItem(updatedItem)
    }
  }

  override suspend fun unfreezeInstance(instanceId: String) {
    itemDao.getItem(instanceId)?.let { item ->
      val pausedDate = item.pausedDate
      val remainingDays = item.remainingDays

      if (pausedDate != null && remainingDays != null) {
        // Calculate new expiration date: now + remaining days
        val now = appClock.now()
        val newExpirationDate = now.toEpochMilliseconds() + (remainingDays.days.inWholeMilliseconds)

        val updatedItem = item.copy(
          expirationDate = newExpirationDate,
          pausedDate = null,
          remainingDays = null
        )
        itemDao.updateItem(updatedItem)
      }
    }
  }

  override suspend fun clearAllProducts() {
    categoryDao.clearAllCategories()
  }

  override suspend fun clearAllInstances() {
    itemDao.clearAllItems()
  }
}