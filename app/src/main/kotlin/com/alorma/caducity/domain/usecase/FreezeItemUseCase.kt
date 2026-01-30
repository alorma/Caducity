package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import kotlin.time.Instant

class FreezeItemUseCase(
  private val categoryDataSource: CategoryDataSource,
  private val appClock: AppClock,
) {

  suspend fun freezeItem(
    itemId: String,
    expirationDate: Instant,
  ): InstanceActionResult<Unit> {
    // Calculate remaining days from now to expiration
    val now = appClock.now()
    val nowDate = now.date()
    val expirationLocalDate = expirationDate.date()

    val remainingDays = (expirationLocalDate.toEpochDays() - nowDate.toEpochDays()).toInt()

    // Only freeze if not already expired (allow freezing items expiring today)
    return if (remainingDays >= 0) {
      categoryDataSource.freezeItem(itemId, remainingDays)
      InstanceActionResult.Success(Unit)
    } else {
      InstanceActionResult.Failure(InstanceActionError.CannotFreezeExpiredInstance)
    }
  }

  suspend fun unfreezeItem(itemId: String) {
    categoryDataSource.unfreezeItem(itemId)
  }
}
