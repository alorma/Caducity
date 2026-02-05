package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource

class DeleteCategoryUseCase(
  private val categoryDataSource: CategoryDataSource,
) {

  suspend fun deleteCategory(categoryId: String): Result<Unit> {
    return try {
      categoryDataSource.deleteCategory(categoryId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
