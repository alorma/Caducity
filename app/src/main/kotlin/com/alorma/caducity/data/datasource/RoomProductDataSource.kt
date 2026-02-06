package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.RoomEntityMapper
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
  private val mapper: RoomEntityMapper,
) : ProductDataSource {

  private val productDao = database.productDao()

  override fun getProductsByCategory(categoryId: String): Flow<ImmutableList<Product>> {
    return productDao.getProductsByCategory(categoryId)
      .map { entities ->
        entities.map { mapper.mapProductToModel(it) }.toImmutableList()
      }
  }

  override suspend fun createProduct(categoryId: String, name: String): Product {
    val product = Product(
      id = UUID.randomUUID().toString(),
      categoryId = categoryId,
      name = name,
      createdAt = appClock.now(),
    )
    productDao.insertProduct(mapper.mapProductToEntity(product))
    return product
  }
}
