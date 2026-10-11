package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ItemSnapshot
import kotlinx.coroutines.CancellationException

/** Undo of consume/discard/freeze/reschedule. */
class RestoreItemsUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  /** Captures the state of the units before a bulk action; undo it with [restoreSnapshots]. */
  suspend fun snapshots(itemIds: List<String>): List<ItemSnapshot> = pantryDataSource.snapshots(itemIds)

  /** Puts the units back exactly as they were (a frozen unit comes back frozen). */
  suspend fun restoreSnapshots(snapshots: List<ItemSnapshot>): Result<Unit> =
    try {
      pantryDataSource.restoreSnapshots(snapshots)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

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
