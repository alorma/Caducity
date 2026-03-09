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
import kotlinx.datetime.LocalTime
import java.util.concurrent.TimeUnit
import timber.log.Timber

/**
 * Schedules periodic background work to check for expiring categories.
 * Uses WorkManager to ensure the work runs even when the app is closed.
 * The work is scheduled to run daily at the user-configured time (default: noon).
 */
class ExpirationWorkSchedulerImpl(
  private val context: Context,
  private val delayCalculator: NotificationDelayCalculator,
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
    Timber.tag(TAG).d( "Scheduling expiration check work at ${time.hour}:${time.minute.toString().padStart(2, '0')}...")

    val initialDelay = delayCalculator.calculate(time)
    Timber.tag(TAG).d( "Initial delay: ${initialDelay.inWholeMinutes} minutes")

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

    Timber.tag(TAG).d( "Expiration check work scheduled successfully")
  }

  /**
   * Cancels all scheduled expiration check work.
   */
  override fun cancelExpirationCheck() {
    Timber.tag(TAG).d( "Cancelling expiration check work...")
    WorkManager.getInstance(context).cancelUniqueWork(ExpirationCheckWorker.WORK_NAME)
    Timber.tag(TAG).d( "Expiration check work cancelled")
  }

  /**
   * Triggers an immediate expiration check for testing purposes.
   */
  override fun triggerImmediateCheck() {
    Timber.tag(TAG).d( "Triggering immediate expiration check...")

    val workRequest = OneTimeWorkRequestBuilder<ExpirationCheckWorker>()
      .build()

    WorkManager.getInstance(context).enqueue(workRequest)
    Timber.tag(TAG).d( "Immediate expiration check enqueued")
  }
}
