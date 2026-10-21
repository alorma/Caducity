package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.ItemSnapshot
import kotlinx.coroutines.CancellationException

class ConsumeItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  /** Consumes all the units in one transaction (bulk actions of the status lists). */
  suspend fun consume(itemIds: List<String>): Result<Unit> =
    try {
      pantryDataSource.consume(itemIds, appClock.now())
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  /**
   * The ✓ of a handful product (Grapats): the batch goes down a level, and from the lowest one it is consumed. Returns the
   * state of the batch before, to undo with `RestoreItemsUseCase.restoreSnapshots`.
   */
  suspend fun lowerLevel(itemId: String): Result<ItemSnapshot> =
    try {
      val before =
        pantryDataSource.snapshots(listOf(itemId)).firstOrNull()
          ?: return Result.failure(InstanceActionError.InstanceNotFound)
      pantryDataSource.lowerLevel(itemId, appClock.now())
      Result.success(before)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  /** Permissive consume (P5): any existing item can be consumed, including expired ones. */
  suspend fun consume(itemId: String): Result<Unit> {
    itemDataSource.getItem(itemId) ?: return Result.failure(InstanceActionError.InstanceNotFound)
    itemDataSource.markItemAsConsumed(itemId)
    return Result.success(Unit)
  }
}
