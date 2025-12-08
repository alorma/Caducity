package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import kotlinx.coroutines.flow.Flow

interface ProductDataSource {
  fun getAllProducts(): Flow<List<Product>>
  suspend fun getProductById(id: String): Product?
  suspend fun insertProduct(product: Product)
  suspend fun deleteProduct(id: String)
  suspend fun updateProduct(product: Product)

  fun getAllProductInstances(): Flow<List<ProductInstance>>
  fun getProductInstancesByProductId(productId: String): Flow<List<ProductInstance>>
  suspend fun getProductInstanceById(id: String): ProductInstance?
  suspend fun insertProductInstance(instance: ProductInstance)
  suspend fun deleteProductInstance(id: String)
  suspend fun updateProductInstance(instance: ProductInstance)
}
