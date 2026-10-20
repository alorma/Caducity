package com.alorma.caducity.feature.notification.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import com.alorma.caducity.feature.notification.NotificationContentBuilder
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * WorkManager worker that runs once a day at the reminder time and shows the daily reminders
 * («N productes caduquen aviat», and one for what expires today) only when there is something to say.
 *
 * Uses Koin for dependency injection - dependencies are injected via Koin's inject() delegate.
 */
class ExpirationCheckWorker(
  context: Context,
  params: WorkerParameters,
  private val observeItems: ObserveItemsUseCase,
  private val pantryDataSource: PantryDataSource,
  private val todayProvider: TodayProvider,
  private val notificationConfig: NotificationConfigDataSource,
  private val notificationHelper: ExpirationNotificationHelper,
) : CoroutineWorker(context, params) {
  companion object {
    private const val TAG = "ExpirationCheckWorker"
    const val WORK_NAME = "expiration_check_work"
  }

  override suspend fun doWork(): Result =
    try {
      if (notificationConfig.isNotificationsEnabled()) {
        val reminders =
          NotificationContentBuilder.build(
            entries = observeItems.observe().first(),
            today = todayProvider.today(),
            categoryNames = pantryDataSource.observeCategories().first().associate { it.id to it.name },
          )
        Timber.tag(TAG).d(
          "Daily reminders: ${reminders.soon?.products?.size ?: 0} soon, ${reminders.today?.products?.size ?: 0} today",
        )
        notificationHelper.showDailyReminders(reminders)
      }
      Result.success()
    } catch (e: Exception) {
      Timber.tag(TAG).e(e)
      Result.retry()
    }
}
