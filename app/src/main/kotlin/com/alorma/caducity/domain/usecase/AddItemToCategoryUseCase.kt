package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.NewItem
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

class AddItemToCategoryUseCase(
  private val categoryDataSource: CategoryDataSource,
) {

  @OptIn(ExperimentalUuidApi::class)
  suspend fun addItem(
    categoryId: String,
    identifier: String,
    categoryId: String? = null,
    expirationDate: Instant,
  ): Result<String> {
    return try {
      val item = NewItem(
        identifier = identifier,
        categoryId = categoryId,
        expirationDate = expirationDate,
      )

      val createdItem = categoryDataSource.addItem(categoryId, item)
      Result.success(createdItem)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
