package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.toModel
import com.alorma.caducity.data.datasource.room.toRoomEntity
import com.alorma.caducity.domain.ProductDataSource
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
        val startOfDayMillis = filter.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val nextDayMillis = startOfDayMillis + 1.days.inWholeMilliseconds
        productDao.getProductsWithInstancesByDate(startOfDayMillis, nextDayMillis)
      }
      is ProductsListFilter.ByDateRange -> {
        val startMillis = filter.startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = filter.endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() + 1.days.inWholeMilliseconds
        productDao.getProductsWithInstancesByDateRange(startMillis, endMillis)
      }
      is ProductsListFilter.ByStatus -> {
        // Status filtering needs to be done in memory since it depends on current time
        // and business logic in toModel()
        productDao.getAllProductsWithInstances()
      }
    }

    return daoFlow.map { roomEntities ->
      val products = roomEntities.map { it.toModel(appClock, expirationThresholds) }

      // Apply status filter in memory if needed
      val filtered = if (filter is ProductsListFilter.ByStatus) {
        products.filter { productWithInstances ->
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
}