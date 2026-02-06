package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ItemDataSource

class DeleteItemUseCase(
  private val itemDataSource: ItemDataSource,
) {

  suspend fun deleteItem(itemId: String): Result<Unit> {
    return try {
      itemDataSource.deleteItem(itemId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
