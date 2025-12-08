package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductDataSource {
  fun getAllProducts(): Flow<List<Product>>
  suspend fun getProductById(id: String): Product?
  suspend fun insertProduct(product: Product)
  suspend fun deleteProduct(id: String)
  suspend fun updateProduct(product: Product)
}
