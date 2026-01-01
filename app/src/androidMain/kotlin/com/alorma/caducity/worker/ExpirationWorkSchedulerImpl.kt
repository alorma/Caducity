package com.alorma.caducity.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alorma.caducity.notification.ExpirationWorkScheduler
import java.util.concurrent.TimeUnit

/**
 * Schedules periodic background work to check for expiring products.
 * Uses WorkManager to ensure the work runs even when the app is closed.
 */
class ExpirationWorkSchedulerImpl(
  private val context: Context
): ExpirationWorkScheduler {

  companion object {
    private const val TAG = "ExpirationWorkScheduler"
  }

  /**
   * Schedules periodic expiration checks.
   * Work will run once every 24 hours with a 15-minute flex period.
   * Uses KEEP policy to avoid rescheduling if work is already scheduled.
   */
  override fun scheduleExpirationCheck() {
    Log.d(TAG, "Scheduling expiration check work...")

    // Define constraints for the work
    val constraints = Constraints.Builder()
      .setRequiresBatteryNotLow(true) // Don't run if battery is low
      .setRequiresStorageNotLow(true) // Don't run if storage is low
      .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No network needed
      .build()

    // Create periodic work request
    // Runs once per day with a 15-minute flex period for battery optimization
    val workRequest = PeriodicWorkRequestBuilder<ExpirationCheckWorker>(
      repeatInterval = 24, // Every 24 hours
      repeatIntervalTimeUnit = TimeUnit.HOURS,
      flexTimeInterval = 15, // Flex period of 15 minutes
      flexTimeIntervalUnit = TimeUnit.MINUTES
    )
      .setConstraints(constraints)
      .build()

    // Schedule the work
    // KEEP policy ensures we don't duplicate work if it's already scheduled
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      ExpirationCheckWorker.WORK_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )

    Log.d(TAG, "Expiration check work scheduled successfully")
  }

  /**
   * Cancels all scheduled expiration check work.
   * Useful for testing or when user disables notifications.
   */
  override fun cancelExpirationCheck() {
    Log.d(TAG, "Cancelling expiration check work...")
    WorkManager.getInstance(context).cancelUniqueWork(ExpirationCheckWorker.WORK_NAME)
    Log.d(TAG, "Expiration check work cancelled")
  }

  /**
   * Triggers an immediate expiration check for testing purposes.
   * This will run the worker immediately without waiting for the scheduled time.
   */
  override fun triggerImmediateCheck() {
    Log.d(TAG, "Triggering immediate expiration check...")

    val workRequest = androidx.work.OneTimeWorkRequestBuilder<ExpirationCheckWorker>()
      .build()

    WorkManager.getInstance(context).enqueue(workRequest)
    Log.d(TAG, "Immediate expiration check enqueued")
  }
}
