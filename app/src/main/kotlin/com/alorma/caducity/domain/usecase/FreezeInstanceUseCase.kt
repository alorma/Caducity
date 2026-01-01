package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.time.clock.AppClock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class FreezeInstanceUseCase(
  private val productDataSource: ProductDataSource,
  private val appClock: AppClock,
) {

  suspend fun freezeInstance(instanceId: String, expirationDate: Instant): Result<Unit> {
    return try {
      // Calculate remaining days from now to expiration
      val now = appClock.now()
      val nowDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
      val expirationLocalDate = expirationDate.toLocalDateTime(TimeZone.currentSystemDefault()).date

      val remainingDays = (expirationLocalDate.toEpochDays() - nowDate.toEpochDays()).toInt()

      // Only freeze if there are remaining days (prevent freezing already expired items)
      if (remainingDays > 0) {
        productDataSource.freezeInstance(instanceId, remainingDays)
        Result.success(Unit)
      } else {
        Result.failure(IllegalStateException("Cannot freeze expired or today-expiring items"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun unfreezeInstance(instanceId: String): Result<Unit> {
    return try {
      productDataSource.unfreezeInstance(instanceId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
