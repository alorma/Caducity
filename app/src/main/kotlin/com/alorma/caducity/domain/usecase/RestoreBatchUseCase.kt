package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.BatchSnapshot
import kotlinx.coroutines.CancellationException

/** Undo of [RemoveBatchEntryUseCase]: the units come back with the same ids and dates. */
class RestoreBatchUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun restore(snapshot: BatchSnapshot): Result<Unit> =
    try {
      pantryDataSource.restoreBatch(snapshot)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
