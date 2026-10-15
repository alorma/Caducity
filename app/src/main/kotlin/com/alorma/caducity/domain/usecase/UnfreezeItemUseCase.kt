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
}
