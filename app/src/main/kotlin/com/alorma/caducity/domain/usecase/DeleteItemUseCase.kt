package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource

class DeleteItemUseCase(
  private val categoryDataSource: CategoryDataSource,
) {

  suspend fun deleteItem(itemId: String): Result<Unit> {
    return try {
      categoryDataSource.deleteItem(itemId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
