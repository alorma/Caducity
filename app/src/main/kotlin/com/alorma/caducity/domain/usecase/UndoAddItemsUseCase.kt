package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.AddedBatch
import kotlinx.coroutines.CancellationException

class UndoAddItemsUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun undo(batch: AddedBatch): Result<Unit> =
    try {
      pantryDataSource.undoAdd(batch)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
