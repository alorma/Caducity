package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.FrozenAmount
import com.alorma.caducity.domain.model.ItemSnapshot
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.UnitType
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

/** What [FreezeAmountUseCase.freeze] did, with what [FreezeAmountUseCase.undo] needs. */
data class FreezeAmountOutcome(
  val frozen: FrozenAmount,
  val before: ItemSnapshot,
)

/** Freezes part of a measured batch (g or mL). */
class FreezeAmountUseCase(
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  /** Fails with [PantryError.CannotFreeze] when the batch is expired, already frozen or of a product that cannot be frozen. */
  suspend fun freeze(
    itemId: String,
    amount: Int,
  ): Result<FreezeAmountOutcome> {
    if (amount !in 1..UnitType.MAX_AMOUNT) return Result.failure(PantryError.InvalidAmount(amount))
    return try {
      val before =
        pantryDataSource.snapshots(listOf(itemId)).firstOrNull() ?: return Result.failure(PantryError.ItemNotFound)
      val frozen =
        pantryDataSource.freezeAmount(itemId, amount, appClock.now()) ?: return Result.failure(PantryError.CannotFreeze)
      Result.success(FreezeAmountOutcome(frozen, before))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /** Thaws [amount] of a frozen batch, which gets today plus three days (like the units). Undo it with [undo]. */
  suspend fun unfreeze(
    itemId: String,
    amount: Int,
  ): Result<FreezeAmountOutcome> {
    if (amount !in 1..UnitType.MAX_AMOUNT) return Result.failure(PantryError.InvalidAmount(amount))
    return try {
      val before =
        pantryDataSource.snapshots(listOf(itemId)).firstOrNull() ?: return Result.failure(PantryError.ItemNotFound)
      val newExpiration =
        appClock
          .nowDate()
          .plus(
            UnfreezeItemUseCase.UNFREEZE_DAYS,
            DateTimeUnit.DAY,
          ).toExpirationInstant()
      val thawed =
        pantryDataSource.unfreezeAmount(itemId, amount, newExpiration)
          ?: return Result.failure(PantryError.CannotFreeze)
      Result.success(FreezeAmountOutcome(thawed, before))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /** Puts the batch back as it was and removes the frozen part that was split off. */
  suspend fun undo(outcome: FreezeAmountOutcome): Result<Unit> =
    try {
      if (outcome.frozen.created) pantryDataSource.deleteItems(listOf(outcome.frozen.itemId))
      pantryDataSource.restoreSnapshots(listOf(outcome.before))
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
