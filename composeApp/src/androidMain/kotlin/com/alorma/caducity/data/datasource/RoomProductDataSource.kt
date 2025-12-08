package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
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

  override fun getAllProductInstances(): Flow<List<ProductInstance>> = flowOf(emptyList())

  override fun getProductInstancesByProductId(productId: String): Flow<List<ProductInstance>> =
    flowOf(emptyList())

  override suspend fun getProductInstanceById(id: String): ProductInstance? = null

  override suspend fun insertProductInstance(instance: ProductInstance) {
    // TODO: Implement Room insert
  }

  override suspend fun deleteProductInstance(id: String) {
    // TODO: Implement Room delete
  }

  override suspend fun updateProductInstance(instance: ProductInstance) {
    // TODO: Implement Room update
  }
}
