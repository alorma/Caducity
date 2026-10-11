package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.normalizeForSearch
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.toImmutableList

class CreateCategoryUseCase(
  private val categoryDataSource: CategoryDataSource,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
  private val pantryDataSource: PantryDataSource,
) {
  @OptIn(ExperimentalUuidApi::class)
  suspend fun createCategory(
    name: String,
    description: String,
    // TEMP(fase 8): the new create screen has no initial items.
    items: List<Pair<String, Instant>> = emptyList(),
    shape: CategoryShape = CategoryShape.COOKIE,
    icon: CategoryIcon = CategoryIcon.DEFAULT,
    sortOrder: Int? = null,
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
        )

      val now = appClock.now()
      val categoryItems =
        items
          .map { (identifier, expirationDate) ->
            Item(
              id = Uuid.random().toString(),
              categoryId = categoryId,
              identifier = identifier,
              expirationDate = expirationDate,
              status =
                ItemStatus.calculateStatus(
                  expirationDate = expirationDate,
                  now = now,
                  soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
                ),
            )
          }.toImmutableList()

      categoryDataSource.createCategory(category, categoryItems)
      Result.success(categoryId)
    } catch (e: Exception) {
      Result.failure(e)
    }
}
