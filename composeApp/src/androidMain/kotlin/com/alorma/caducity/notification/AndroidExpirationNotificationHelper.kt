package com.alorma.caducity.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.alorma.caducity.MainActivity
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ProductWithInstances

/**
 * Android implementation of ExpirationNotificationHelper.
 * Builds and shows notifications using Android's notification system.
 */
class AndroidExpirationNotificationHelper(
  private val context: Context
) : ExpirationNotificationHelper {

  companion object {
    private const val NOTIFICATION_ID = 1001
  }

  override fun showExpirationNotification(expiringProducts: List<ProductWithInstances>) {
    if (expiringProducts.isEmpty()) {
      return
    }

    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create intent to open app with filtered view
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(ExpirationNotificationHelper.EXTRA_SHOW_EXPIRING_ONLY, true)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Build notification
    val notification =
      NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID_EXPIRATION)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(buildNotificationTitle(expiringProducts.size))
        .setContentText(buildNotificationText(expiringProducts))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    notificationManager.notify(NOTIFICATION_ID, notification)
  }

  private fun buildNotificationTitle(count: Int): String {
    return if (count == 1) {
      "Product expiring soon"
    } else {
      "$count products expiring soon"
    }
  }

  private fun buildNotificationText(products: List<ProductWithInstances>): String {
    return if (products.size == 1) {
      products.first().product.name
    } else {
      "${products.first().product.name} and ${products.size - 1} more"
    }
  }
}
