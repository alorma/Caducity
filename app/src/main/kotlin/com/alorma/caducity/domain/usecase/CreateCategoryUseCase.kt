package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateCategoryUseCase(
  private val categoryDataSource: CategoryDataSource,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {

  @OptIn(ExperimentalUuidApi::class)
  suspend fun createCategory(
    name: String,
    description: String,
    items: List<Pair<String, Instant>>,
  ): Result<String> {
    return try {
      val categoryId = Uuid.random().toString()
      val category = Category(
        id = categoryId,
        name = name,
        description = description,
      )

      val now = appClock.now()
      val categoryItems = items.map { (identifier, expirationDate) ->
        Item(
          id = Uuid.random().toString(),
          identifier = identifier,
          expirationDate = expirationDate,
          status = ItemStatus.calculateStatus(
            expirationDate = expirationDate,
            now = now,
            soonExpiringThreshold = expirationThresholds.soonExpiringThreshold
          ),
        )
      }.toImmutableList()

      categoryDataSource.createCategory(category, categoryItems)
      Result.success(categoryId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
