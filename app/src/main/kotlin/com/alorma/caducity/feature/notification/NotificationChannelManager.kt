package com.alorma.caducity.feature.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Manages notification channels for the app.
 * Notification channels are required on Android O (API 26) and above.
 */
object NotificationChannelManager {

  const val CHANNEL_ID_EXPIRING_SOON = "product_expiring_soon"
  const val CHANNEL_ID_EXPIRED = "product_expired"

  /**
   * Creates all notification channels for the app.
   * This method is safe to call multiple times - channels will only be created once.
   *
   * @param context Application or Activity context
   */
  fun createNotificationChannels(context: Context) {
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val expiringSoonChannel = NotificationChannel(
      CHANNEL_ID_EXPIRING_SOON,
      "Expiring Soon Alerts",
      NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
      description = "Notifications for categories with items approaching their expiration date"
      enableVibration(true)
      enableLights(true)
    }

    val expiredChannel = NotificationChannel(
      CHANNEL_ID_EXPIRED,
      "Expired Alerts",
      NotificationManager.IMPORTANCE_HIGH
    ).apply {
      description = "Notifications for categories with items that have already expired"
      enableVibration(true)
      enableLights(true)
    }

    notificationManager.createNotificationChannel(expiringSoonChannel)
    notificationManager.createNotificationChannel(expiredChannel)
  }
}
