package com.alorma.caducity.feature.notification.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alorma.caducity.domain.usecase.GetExpiringCategoriesUseCase
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager worker that checks for expiring categories and shows notifications.
 * Runs periodically in the background to notify users about categories with items expiring soon.
 *
 * Uses Koin for dependency injection - dependencies are injected via Koin's inject() delegate.
 */
class ExpirationCheckWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

  private val getExpiringCategoriesUseCase: GetExpiringCategoriesUseCase by inject()
  private val notificationHelper: ExpirationNotificationHelper by inject()

  companion object {
    private const val TAG = "ExpirationCheckWorker"
    const val WORK_NAME = "expiration_check_work"
  }

  override suspend fun doWork(): Result {
    return try {
      Log.d(TAG, "Starting expiration check...")

      // Get categories with items expiring soon
      val expiringCategories = getExpiringCategoriesUseCase.load()

      Log.d(TAG, "Found ${expiringCategories.size} expiring categories")

      // Show notification if there are expiring categories
      if (expiringCategories.isNotEmpty()) {
        notificationHelper.showExpirationNotification(expiringCategories)
        Log.d(TAG, "Notification shown for ${expiringCategories.size} categories")
      } else {
        Log.d(TAG, "No expiring categories, skipping notification")
      }

      Result.success()
    } catch (e: Exception) {
      Log.e(TAG, "Error checking expiring categories", e)
      Result.retry()
    }
  }
}
