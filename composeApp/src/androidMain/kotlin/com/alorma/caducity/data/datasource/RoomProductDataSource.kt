package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.data.room.AppDatabase
import com.alorma.caducity.data.room.toModel
import com.alorma.caducity.data.room.toRoomEntity
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.time.clock.AppClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RoomProductDataSource(
  database: AppDatabase,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) : ProductDataSource {

  private val productDao = database.productDao()
  private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override val products: StateFlow<ImmutableList<ProductWithInstances>> =
    productDao.getAllProductsWithInstances()
      .map { roomEntities ->
        roomEntities.map { it.toModel(appClock, expirationThresholds) }.toImmutableList()
      }
      .stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = persistentListOf()
      )

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
