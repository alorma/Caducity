package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.InstanceActionResult
import com.alorma.caducity.config.clock.AppClock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class FreezeInstanceUseCase(
  private val productDataSource: ProductDataSource,
  private val appClock: AppClock,
) {

  suspend fun freezeInstance(
    instanceId: String,
    expirationDate: Instant,
  ): InstanceActionResult<Unit> {
    // Calculate remaining days from now to expiration
    val now = appClock.now()
    val nowDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val expirationLocalDate = expirationDate.toLocalDateTime(TimeZone.currentSystemDefault()).date

    val remainingDays = (expirationLocalDate.toEpochDays() - nowDate.toEpochDays()).toInt()

    // Only freeze if not already expired (allow freezing items expiring today)
    return if (remainingDays >= 0) {
      productDataSource.freezeInstance(instanceId, remainingDays)
      InstanceActionResult.Success(Unit)
    } else {
      InstanceActionResult.Failure(InstanceActionError.CannotFreezeExpiredInstance)
    }
  }

  suspend fun unfreezeInstance(instanceId: String) {
    productDataSource.unfreezeInstance(instanceId)
  }
}
