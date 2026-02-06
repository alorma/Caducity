package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.NewItem
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

class AddItemToCategoryUseCase(
  private val itemDataSource: ItemDataSource,
) {

  @OptIn(ExperimentalUuidApi::class)
  suspend fun addItem(
    categoryId: String,
    identifier: String,
    productId: String? = null,
    expirationDate: Instant,
  ): Result<String> {
    return try {
      val item = NewItem(
        identifier = identifier,
        productId = productId,
        expirationDate = expirationDate,
      )

      val createdItem = itemDataSource.addItem(categoryId, item)
      Result.success(createdItem)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
