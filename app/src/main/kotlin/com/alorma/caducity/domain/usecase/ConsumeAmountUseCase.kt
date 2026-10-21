package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ItemSnapshot
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.UnitType
import kotlinx.coroutines.CancellationException

/** Consumes part of a measured batch (g or mL). */
class ConsumeAmountUseCase(
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  /** Returns the state of the batch before, to undo with `RestoreItemsUseCase.restoreSnapshots`. */
  suspend fun consume(
    itemId: String,
    amount: Int,
  ): Result<ItemSnapshot> {
    if (amount !in 1..UnitType.MAX_AMOUNT) return Result.failure(PantryError.InvalidAmount(amount))
    return try {
      val before =
        pantryDataSource.snapshots(listOf(itemId)).firstOrNull() ?: return Result.failure(PantryError.ItemNotFound)
      pantryDataSource.consumeAmount(itemId, amount, appClock.now())
      Result.success(before)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
