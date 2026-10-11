package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.SuggestedThresholds
import kotlinx.coroutines.CancellationException

/** Edits the "soon" margin of categories from the «Marge per categoria» screen. */
class CategoryThresholdsUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  /** Sets the margin of one category; `null` makes it follow the global default. */
  suspend fun set(
    categoryId: String,
    days: Int?,
  ): Result<Unit> =
    attempt {
      val category =
        pantryDataSource.getCategories().firstOrNull { it.id == categoryId }
          ?: throw PantryError.CategoryNotFound
      pantryDataSource.updateCategory(category.copy(soonThresholdDays = days))
    }

  /** Suggested categories get their suggested margin; the others go back to the global default. */
  suspend fun restoreSuggested(): Result<Unit> =
    attempt {
      pantryDataSource.getCategories().forEach { category ->
        pantryDataSource.updateCategory(category.copy(soonThresholdDays = SuggestedThresholds.forName(category.name)))
      }
    }

  private suspend fun attempt(block: suspend () -> Unit): Result<Unit> =
    try {
      block()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
