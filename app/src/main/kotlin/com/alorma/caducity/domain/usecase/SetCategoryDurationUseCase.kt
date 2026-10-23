package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.PantryError
import kotlinx.coroutines.CancellationException

class SetCategoryDurationUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  /** Changes only the category's usual duration; `null` clears it. */
  suspend fun set(
    categoryId: String,
    usualDurationDays: Int?,
  ): Result<Unit> =
    try {
      val current =
        pantryDataSource.getCategories().firstOrNull { it.id == categoryId }
          ?: return Result.failure(PantryError.CategoryNotFound)
      pantryDataSource.updateCategory(current.copy(usualDurationDays = usualDurationDays))
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
