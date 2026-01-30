package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.toModel
import com.alorma.caducity.data.datasource.room.toRoomEntity
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomProductDataSource(
  database: AppDatabase,
  private val appClock: AppClock,
) : ProductDataSource {

  private val productDao = database.productDao()

  override fun getProductsByCategory(categoryId: String): Flow<ImmutableList<Product>> {
    return productDao.getProductsByCategory(categoryId)
      .map { entities ->
        entities.map { it.toModel() }.toImmutableList()
      }
  }

  override suspend fun getProduct(categoryId: String): Product? {
    return productDao.getProduct(categoryId)?.toModel()
  }

  override suspend fun createProduct(categoryId: String, name: String): Product {
    val product = Product(
      id = UUID.randomUUID().toString(),
      categoryId = categoryId,
      name = name,
      createdAt = appClock.now(),
    )
    productDao.insertProduct(product.toRoomEntity())
    return product
  }

  override suspend fun deleteProduct(categoryId: String): Result<Unit> {
    val itemCount = productDao.getActiveItemCount(categoryId)
    return if (itemCount > 0) {
      Result.failure(IllegalStateException("Cannot delete product with active items"))
    } else {
      productDao.deleteProduct(categoryId)
      Result.success(Unit)
    }
  }

  override suspend fun getActiveItemCount(categoryId: String): Int {
    return productDao.getActiveItemCount(categoryId)
  }

  override suspend fun clearAllProducts() {
    productDao.clearAllProducts()
  }
}
