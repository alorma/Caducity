package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.SuggestedDurations
import kotlinx.coroutines.CancellationException

/** Edits the usual duration of categories from the «Marge per categoria» screen. */
class CategoryDurationsUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  /** Sets the usual duration of one category; `null` clears it. */
  suspend fun set(
    categoryId: String,
    days: Int?,
  ): Result<Unit> =
    attempt {
      val category =
        pantryDataSource.getCategories().firstOrNull { it.id == categoryId }
          ?: throw PantryError.CategoryNotFound
      pantryDataSource.updateCategory(category.copy(usualDurationDays = days))
    }

  /** Suggested categories get their suggested duration; the others are cleared. */
  suspend fun restoreSuggested(): Result<Unit> =
    attempt {
      pantryDataSource.getCategories().forEach { category ->
        pantryDataSource.updateCategory(category.copy(usualDurationDays = SuggestedDurations.forName(category.name)))
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
