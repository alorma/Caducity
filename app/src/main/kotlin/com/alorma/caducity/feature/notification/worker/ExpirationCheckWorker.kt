package com.alorma.caducity.feature.notification.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alorma.caducity.domain.usecase.GetExpiringCategoriesUseCase
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * WorkManager worker that checks for expiring categories and shows notifications.
 * Runs periodically in the background to notify users about categories with items expiring soon.
 *
 * Uses Koin for dependency injection - dependencies are injected via Koin's inject() delegate.
 */
class ExpirationCheckWorker(
  context: Context,
  params: WorkerParameters,
  private val getExpiringCategoriesUseCase: GetExpiringCategoriesUseCase,
  private val notificationHelper: ExpirationNotificationHelper,
) : CoroutineWorker(context, params) {

  companion object {
    private const val TAG = "ExpirationCheckWorker"
    const val WORK_NAME = "expiration_check_work"
  }

  override suspend fun doWork(): Result {
    return try {
      Timber.tag(TAG).d("Starting expiration check...")

      // Get categories with items expiring soon
      val expiringCategories = getExpiringCategoriesUseCase.load()

      Timber.tag(TAG).d("Found ${expiringCategories.size} expiring categories")

      // Show notification if there are expiring categories
      if (expiringCategories.isNotEmpty()) {
        notificationHelper.showExpirationNotification(expiringCategories)
        Timber.tag(TAG).d("Notification shown for ${expiringCategories.size} categories")
      } else {
        Timber.tag(TAG).d("No expiring categories, skipping notification")
      }

      Result.success()
    } catch (e: Exception) {
      Timber.tag(TAG).e(e)
      Result.retry()
    }
  }
}
