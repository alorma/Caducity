package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface ProductDataSource {

  fun getProductsByCategory(categoryId: String): Flow<ImmutableList<Product>>

  suspend fun getProduct(categoryId: String): Product?

  suspend fun createProduct(categoryId: String, name: String): Product

  suspend fun deleteProduct(categoryId: String): Result<Unit>

  suspend fun getActiveItemCount(categoryId: String): Int

  suspend fun clearAllProducts()
}
