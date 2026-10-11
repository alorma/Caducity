package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import kotlinx.coroutines.CancellationException

/** Undo of consume/discard: the items become active again. */
class RestoreItemsUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  suspend fun restore(itemIds: List<String>): Result<Unit> =
    try {
      pantryDataSource.restore(itemIds)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
