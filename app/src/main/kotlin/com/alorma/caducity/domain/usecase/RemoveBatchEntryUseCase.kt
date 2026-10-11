package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.AddedBatch
import com.alorma.caducity.domain.model.BatchSnapshot
import kotlinx.coroutines.CancellationException

/** «Treu de la tanda»: deletes the units of an entry and returns what is needed to undo it. */
class RemoveBatchEntryUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun remove(batch: AddedBatch): Result<BatchSnapshot> =
    try {
      Result.success(pantryDataSource.removeBatch(batch))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
