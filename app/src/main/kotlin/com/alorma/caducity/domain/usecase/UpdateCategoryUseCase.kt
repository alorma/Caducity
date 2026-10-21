package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.normalizeForSearch
import kotlinx.coroutines.CancellationException

class UpdateCategoryUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun update(
    categoryId: String,
    name: String,
    shape: CategoryShape,
    icon: CategoryIcon,
    /** `null` clears the usual duration. */
    usualDurationDays: Int?,
    /** `null` keeps the stored description. */
    description: String? = null,
    /** `null` keeps the stored value. */
    canFreeze: Boolean? = null,
  ): Result<Unit> {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return Result.failure(PantryError.BlankName)
    return try {
      val categories = pantryDataSource.getCategories()
      val current =
        categories.firstOrNull { it.id == categoryId } ?: return Result.failure(PantryError.CategoryNotFound)
      val duplicated =
        categories.any {
          it.id != categoryId &&
            it.name.normalizeForSearch() == trimmed.normalizeForSearch()
        }
      if (duplicated) return Result.failure(PantryError.DuplicateCategoryName)

      pantryDataSource.updateCategory(
        current.copy(
          name = trimmed,
          shape = shape,
          icon = icon,
          usualDurationDays = usualDurationDays,
          description = description?.trim() ?: current.description,
          canFreeze = canFreeze ?: current.canFreeze,
        ),
      )
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
