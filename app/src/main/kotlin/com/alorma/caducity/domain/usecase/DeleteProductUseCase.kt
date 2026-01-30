package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource

class DeleteProductUseCase(
  private val productDataSource: ProductDataSource,
) {
  suspend operator fun invoke(categoryId: String): Result<Unit> {
    return productDataSource.deleteProduct(categoryId)
  }
}
