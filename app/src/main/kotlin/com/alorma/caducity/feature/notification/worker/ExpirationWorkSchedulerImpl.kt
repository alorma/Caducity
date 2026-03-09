package com.alorma.caducity.feature.notification.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alorma.caducity.feature.notification.ExpirationWorkScheduler
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import java.util.concurrent.TimeUnit

/**
 * Schedules periodic background work to check for expiring categories.
 * Uses WorkManager to ensure the work runs even when the app is closed.
 * The work is scheduled to run daily at the user-configured time (default: noon).
 */
class ExpirationWorkSchedulerImpl(
  private val context: Context,
) : ExpirationWorkScheduler {

  companion object {
    private const val TAG = "ExpirationWorkScheduler"
  }

  /**
   * Schedules periodic expiration checks at [time].
   * Uses KEEP policy so the schedule is not reset on every app startup.
   */
  override fun scheduleExpirationCheck(time: LocalTime) {
    enqueueWork(time, ExistingPeriodicWorkPolicy.KEEP)
  }

  /**
   * Cancels existing work and reschedules at [time].
   * Called when the user changes their notification time preference.
   */
  override fun rescheduleExpirationCheck(time: LocalTime) {
    enqueueWork(time, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE)
  }

  private fun enqueueWork(time: LocalTime, policy: ExistingPeriodicWorkPolicy) {
    Log.d(TAG, "Scheduling expiration check work at ${time.hour}:${time.minute.toString().padStart(2, '0')}...")

    val initialDelay = calculateInitialDelay(time)
    Log.d(TAG, "Initial delay: ${initialDelay.inWholeMinutes} minutes")

    val constraints = Constraints.Builder()
      .setRequiresBatteryNotLow(true)
      .setRequiresStorageNotLow(true)
      .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
      .build()

    val workRequest = PeriodicWorkRequestBuilder<ExpirationCheckWorker>(
      repeatInterval = 24,
      repeatIntervalTimeUnit = TimeUnit.HOURS,
      flexTimeInterval = 15,
      flexTimeIntervalUnit = TimeUnit.MINUTES,
    )
      .setInitialDelay(initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
      .setConstraints(constraints)
      .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      ExpirationCheckWorker.WORK_NAME,
      policy,
      workRequest,
    )

    Log.d(TAG, "Expiration check work scheduled successfully")
  }

  /**
   * Calculates the delay until the next occurrence of [targetTime] in the system timezone.
   * If [targetTime] is still in the future today, returns the duration until then.
   * Otherwise, returns the duration until [targetTime] tomorrow.
   */
  private fun calculateInitialDelay(targetTime: LocalTime): Duration {
    val timezone = TimeZone.currentSystemDefault()
    val now = Clock.System.now()
    val nowLocal = now.toLocalDateTime(timezone)

    val targetToday = LocalDateTime(nowLocal.date, targetTime)
    val targetTodayInstant = targetToday.toInstant(timezone)

    return if (targetTodayInstant > now) {
      targetTodayInstant - now
    } else {
      targetTodayInstant + 1.days - now
    }
  }

  /**
   * Cancels all scheduled expiration check work.
   */
  override fun cancelExpirationCheck() {
    Log.d(TAG, "Cancelling expiration check work...")
    WorkManager.getInstance(context).cancelUniqueWork(ExpirationCheckWorker.WORK_NAME)
    Log.d(TAG, "Expiration check work cancelled")
  }

  /**
   * Triggers an immediate expiration check for testing purposes.
   */
  override fun triggerImmediateCheck() {
    Log.d(TAG, "Triggering immediate expiration check...")

    val workRequest = OneTimeWorkRequestBuilder<ExpirationCheckWorker>()
      .build()

    WorkManager.getInstance(context).enqueue(workRequest)
    Log.d(TAG, "Immediate expiration check enqueued")
  }
}
