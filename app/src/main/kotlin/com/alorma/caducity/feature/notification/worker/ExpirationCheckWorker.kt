package com.alorma.caducity.feature.notification.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alorma.caducity.domain.usecase.GetExpiringProductsUseCase
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager worker that checks for expiring products and shows notifications.
 * Runs periodically in the background to notify users about products expiring soon.
 *
 * Uses Koin for dependency injection - dependencies are injected via Koin's inject() delegate.
 */
class ExpirationCheckWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

  private val getExpiringProductsUseCase: GetExpiringProductsUseCase by inject()
  private val notificationHelper: ExpirationNotificationHelper by inject()

  companion object {
    private const val TAG = "ExpirationCheckWorker"
    const val WORK_NAME = "expiration_check_work"
  }

  override suspend fun doWork(): Result {
    return try {
      Log.d(TAG, "Starting expiration check...")

      // Get products expiring soon
      val expiringProducts = getExpiringProductsUseCase.load()

      Log.d(TAG, "Found ${expiringProducts.size} expiring products")

      // Show notification if there are expiring products
      if (expiringProducts.isNotEmpty()) {
        notificationHelper.showExpirationNotification(expiringProducts)
        Log.d(TAG, "Notification shown for ${expiringProducts.size} products")
      } else {
        Log.d(TAG, "No expiring products, skipping notification")
      }

      Result.success()
    } catch (e: Exception) {
      Log.e(TAG, "Error checking expiring products", e)
      Result.retry()
    }
  }
}
