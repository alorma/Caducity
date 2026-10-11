package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException

class FreezeItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val pantryDataSource: PantryDataSource,
  private val appClock: AppClock,
) {
  /** Freezes all the units in one transaction (expired ones are skipped); returns the ids frozen. */
  suspend fun freeze(itemIds: List<String>): Result<List<String>> =
    try {
      Result.success(pantryDataSource.freeze(itemIds, appClock.now()))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }

  suspend fun freezeItem(
    itemId: String,
    expirationDate: Instant,
  ): Result<Unit> {
    // Calculate remaining days from now to expiration
    val now = appClock.now()
    val nowDate = now.date()
    val expirationLocalDate = expirationDate.date()

    val remainingDays = (expirationLocalDate.toEpochDays() - nowDate.toEpochDays()).toInt()

    // Only freeze if not already expired (allow freezing items expiring today)
    return if (remainingDays >= 0) {
      itemDataSource.freezeItem(itemId, remainingDays)
      Result.success(Unit)
    } else {
      Result.failure(InstanceActionError.CannotFreezeExpiredInstance)
    }
  }

  suspend fun unfreezeItem(itemId: String) {
    itemDataSource.unfreezeItem(itemId)
  }
}
