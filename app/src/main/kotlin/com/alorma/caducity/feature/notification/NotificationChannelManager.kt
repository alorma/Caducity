package com.alorma.caducity.feature.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Manages notification channels for the app.
 * Notification channels are required on Android O (API 26) and above.
 */
object NotificationChannelManager {

  const val CHANNEL_ID_EXPIRATION = "product_expiration"

  /**
   * Creates all notification channels for the app.
   * This method is safe to call multiple times - channels will only be created once.
   *
   * @param context Application or Activity context
   */
  fun createNotificationChannels(context: Context) {
    // Notification channels are only supported on Android O and above
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create expiration alerts channel
    val expirationChannel = NotificationChannel(
      CHANNEL_ID_EXPIRATION,
      "Product Expiration Alerts",
      NotificationManager.IMPORTANCE_HIGH
    ).apply {
      description = "Notifications for products that are approaching their expiration date"
      enableVibration(true)
      enableLights(true)
    }

    notificationManager.createNotificationChannel(expirationChannel)
  }
}
