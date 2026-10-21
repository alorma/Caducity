package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.ItemSnapshot
import com.alorma.caducity.domain.model.PantryError
import kotlinx.coroutines.CancellationException

/** Tapping a level in the modal: the batch takes it at once. */
class SetHandfulLevelUseCase(
  private val pantryDataSource: PantryDataSource,
) {
  /** Returns the state of the batch before, to undo with `RestoreItemsUseCase.restoreSnapshots`. */
  suspend fun set(
    itemId: String,
    level: HandfulLevel,
  ): Result<ItemSnapshot> =
    try {
      val before =
        pantryDataSource.snapshots(listOf(itemId)).firstOrNull() ?: return Result.failure(PantryError.ItemNotFound)
      pantryDataSource.setLevel(itemId, level)
      Result.success(before)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
