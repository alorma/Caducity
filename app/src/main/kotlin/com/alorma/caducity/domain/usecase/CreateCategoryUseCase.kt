package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.normalizeForSearch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.persistentListOf

class CreateCategoryUseCase(
  private val categoryDataSource: CategoryDataSource,
  private val pantryDataSource: PantryDataSource,
) {
  @OptIn(ExperimentalUuidApi::class)
  suspend fun createCategory(
    name: String,
    description: String,
    shape: CategoryShape = CategoryShape.COOKIE,
    icon: CategoryIcon = CategoryIcon.DEFAULT,
    sortOrder: Int? = null,
    /** `null` means the category has no usual duration. */
    usualDurationDays: Int? = null,
  ): Result<String> =
    try {
      val duplicated =
        pantryDataSource.getCategories().any { it.name.normalizeForSearch() == name.trim().normalizeForSearch() }
      if (duplicated) return Result.failure(PantryError.DuplicateCategoryName)
      val categoryId = Uuid.random().toString()
      val category =
        Category(
          id = categoryId,
          name = name.trim(),
          description = description,
          shape = shape,
          icon = icon,
          sortOrder = sortOrder ?: pantryDataSource.getNextSortOrder(),
          usualDurationDays = usualDurationDays,
        )

      categoryDataSource.createCategory(category, persistentListOf())
      Result.success(categoryId)
    } catch (e: Exception) {
      Result.failure(e)
    }
}
