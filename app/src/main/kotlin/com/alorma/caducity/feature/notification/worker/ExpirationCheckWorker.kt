package com.alorma.caducity.feature.notification.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alorma.caducity.domain.model.ItemStatus
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

      // Show expired notification if that type is enabled
      if (notificationHelper.areExpiredNotificationsEnabled().value) {
        val expiredCategories = getExpiringCategoriesUseCase.loadByStatus(ItemStatus.Expired)
        Log.d(TAG, "Found ${expiredCategories.size} categories with expired items")
        if (expiredCategories.isNotEmpty()) {
          notificationHelper.showExpirationNotification(expiredCategories, ItemStatus.Expired)
          Log.d(TAG, "Expired notification shown for ${expiredCategories.size} categories")
        }
      }

      // Show expiring soon notification if that type is enabled
      if (notificationHelper.areExpiringSoonNotificationsEnabled().value) {
        val expiringSoonCategories = getExpiringCategoriesUseCase.loadByStatus(ItemStatus.ExpiringSoon)
        Log.d(TAG, "Found ${expiringSoonCategories.size} categories expiring soon")
        if (expiringSoonCategories.isNotEmpty()) {
          notificationHelper.showExpirationNotification(expiringSoonCategories, ItemStatus.ExpiringSoon)
          Log.d(TAG, "Expiring soon notification shown for ${expiringSoonCategories.size} categories")
        }
      }

      Result.success()
    } catch (e: Exception) {
      Log.e(TAG, "Error checking expiring categories", e)
      Result.retry()
    }
  }
}
