package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class UnfreezeItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  /**
   * Unfreezes the units in one transaction: they get [UNFREEZE_DAYS] days from today, so they move to Soon.
   * Returns the ids unfrozen; undo it with a snapshot taken before (it brings back the freezing date).
   */
  suspend fun unfreeze(itemIds: List<String>): Result<List<String>> =
    try {
      val expiration = appClock.nowDate().plus(UNFREEZE_DAYS, DateTimeUnit.DAY).toExpirationInstant()
      Result.success(pantryDataSource.unfreezeAll(itemIds, expiration))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  /** Date proposed when unfreezing (P4): today plus the days that were left when it was frozen. */
  suspend fun proposedDate(itemId: String): LocalDate? {
    val remainingDays = itemDataSource.getItem(itemId)?.remainingDays ?: return null
    return appClock.nowDate().plus(remainingDays, DateTimeUnit.DAY)
  }

  /** Unfreezes the unit giving it the (editable) [newDate]. */
  suspend fun unfreezeOn(
    itemId: String,
    newDate: LocalDate,
  ): Result<Unit> =
    try {
      val item = itemDataSource.getItem(itemId) ?: return Result.failure(InstanceActionError.InstanceNotFound)
      if (item.status != ItemStatus.Frozen) {
        Result.success(Unit)
      } else {
        pantryDataSource.unfreeze(itemId, newDate.toExpirationInstant())
        Result.success(Unit)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  companion object {
    /** Days a unit lasts after being unfrozen (a fixed value for now). */
    const val UNFREEZE_DAYS = 3
  }
}
