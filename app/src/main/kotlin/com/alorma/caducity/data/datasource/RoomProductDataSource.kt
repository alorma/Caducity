package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.dao.ProductDao
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.Product
import java.util.UUID
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomProductDataSource(
  private val productDao: ProductDao,
  private val itemDao: ItemDao,
  private val appClock: AppClock,
  private val productMapper: ProductRoomMapper,
) : ProductDataSource {
  override fun getProductsByCategory(categoryId: String): Flow<ImmutableList<Product>> =
    productDao
      .getProductsByCategory(categoryId)
      .map { entities ->
        entities
          .map { productEntity ->
            productMapper.toModel(productEntity)
          }.toImmutableList()
      }

  override suspend fun createProduct(
    categoryId: String,
    name: String,
  ): Product {
    val product =
      Product(
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

  override suspend fun getActiveItemCount(productId: String): Int = productDao.getActiveItemCount(productId)

  override suspend fun moveItemsToProduct(
    fromProductId: String,
    toProductId: String?,
  ) {
    itemDao.moveItemsToProduct(fromProductId, toProductId)
  }

  override suspend fun getProduct(productId: String): Product? {
    val entity = productDao.getProduct(productId)
    return entity?.let { productMapper.toModel(it) }
  }
}
