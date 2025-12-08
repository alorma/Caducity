package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RoomProductDataSource : ProductDataSource {
  // TODO: Implement Room database
  override fun getAllProducts(): Flow<List<Product>> = flowOf(emptyList())

  override suspend fun getProductById(id: String): Product? = null

  override suspend fun insertProduct(product: Product) {
    // TODO: Implement Room insert
  }

  override suspend fun deleteProduct(id: String) {
    // TODO: Implement Room delete
  }

  override suspend fun updateProduct(product: Product) {
    // TODO: Implement Room update
  }
}
