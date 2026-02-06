package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource

class ClearProductItemsUseCase(
  private val itemDataSource: ItemDataSource,
) {
  suspend fun clearConsumedItems(categoryId: String, productId: String?): Result<Unit> {
    return try {
      itemDataSource.clearConsumedItems(categoryId, productId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun clearAllItems(categoryId: String, productId: String?): Result<Unit> {
    return try {
      itemDataSource.clearAllItems(categoryId, productId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
