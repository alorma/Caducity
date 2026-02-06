package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
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
  private val productMapper: ProductRoomMapper,
) : ProductDataSource {

  private val productDao = database.productDao()

  override fun getProductsByCategory(categoryId: String): Flow<ImmutableList<Product>> {
    return productDao.getProductsByCategory(categoryId)
      .map { entities ->
        entities.map { productEntity ->
          productMapper.toModel(productEntity)
        }.toImmutableList()
      }
  }

  override suspend fun createProduct(categoryId: String, name: String): Product {
    val product = Product(
      id = UUID.randomUUID().toString(),
      categoryId = categoryId,
      name = name,
      createdAt = appClock.now(),
    )
    productDao.insertProduct(productMapper.toEntity(product))
    return product
  }

  override suspend fun deleteProduct(productId: String) {
    val product = productDao.getProduct(productId)
    if (product != null) {
      productDao.deleteProduct(product)
    }
  }

  override suspend fun getActiveItemCount(productId: String): Int {
    return productDao.getActiveItemCount(productId)
  }
}
