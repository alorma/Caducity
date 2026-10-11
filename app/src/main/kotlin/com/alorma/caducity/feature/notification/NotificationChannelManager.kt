package com.alorma.caducity.feature.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.alorma.caducity.R

/**
 * Manages the notification channel of the app: a single one for the daily reminder.
 * Notification channels are required on Android O (API 26) and above.
 */
object NotificationChannelManager {
  const val CHANNEL_ID_DAILY_REMINDER = "daily_reminder"

  // Channels of the versions before the redesign, deleted so they no longer show in the system settings.
  private val LEGACY_CHANNEL_IDS = listOf("product_expiring_soon", "product_expired")

  /** Creates the channel (safe to call many times) and deletes the ones of the old versions. */
  fun createNotificationChannels(context: Context) {
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    LEGACY_CHANNEL_IDS.forEach(notificationManager::deleteNotificationChannel)

    val channel =
      NotificationChannel(
        CHANNEL_ID_DAILY_REMINDER,
        context.getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT,
      ).apply {
        description = context.getString(R.string.notification_channel_description)
      }

    notificationManager.createNotificationChannel(channel)
  }
}
