package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.Product

class CreateProductUseCase(
  private val productDataSource: ProductDataSource,
) {
  suspend fun create(categoryId: String, name: String): Result<Product> {
    if (name.isBlank()) {
      return Result.failure(IllegalArgumentException("Product name cannot be blank"))
    }
    return try {
      val product = productDataSource.createProduct(categoryId, name)
      Result.success(product)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
