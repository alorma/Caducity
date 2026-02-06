package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource

class DeleteProductUseCase(
  private val productDataSource: ProductDataSource,
) {
  suspend fun delete(productId: String): Result<Unit> {
    val activeItemCount = productDataSource.getActiveItemCount(productId)
    if (activeItemCount > 0) {
      return Result.failure(
        IllegalStateException("Cannot delete product with $activeItemCount active items")
      )
    }

    return try {
      productDataSource.deleteProduct(productId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
