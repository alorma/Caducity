package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.DeletedItem
import kotlinx.coroutines.CancellationException

/** Deletes one unit for good (an entry made by mistake: it does not count as waste); [undo] puts it back. */
class DeleteItemUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun delete(itemId: String): Result<DeletedItem> =
    try {
      pantryDataSource.deleteItem(itemId)?.let { Result.success(it) }
        ?: Result.failure(IllegalStateException("Item $itemId not found"))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  suspend fun undo(item: DeletedItem): Result<Unit> =
    try {
      pantryDataSource.restoreDeleted(item)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
