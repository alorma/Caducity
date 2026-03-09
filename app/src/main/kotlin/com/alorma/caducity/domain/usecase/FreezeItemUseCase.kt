package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import kotlin.time.Instant

class FreezeItemUseCase(
  private val itemDataSource: ItemDataSource,
  private val appClock: AppClock,
) {
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
