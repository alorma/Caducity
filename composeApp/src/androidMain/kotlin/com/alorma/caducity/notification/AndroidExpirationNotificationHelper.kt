package com.alorma.caducity.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
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

  private val notifications: MutableState<Boolean> = mutableStateOf(false)

  private fun checkNotificationPermission(): Boolean {
    return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
  }

  override fun areNotificationsEnabled(): MutableState<Boolean> {
    // Update state to reflect actual permission status
    notifications.value = checkNotificationPermission()
    return notifications
  }

  override fun setNotificationsEnabled(enabled: Boolean): Boolean {
    if (enabled) {
      // Can only enable if permission is granted
      val hasPermission = checkNotificationPermission()
      if (hasPermission) {
        notifications.value = true
        return true
      }
      return false
    } else {
      // Can always disable
      notifications.value = false
      return true
    }
  }

  override fun hasNotificationPermission(): Boolean {
    return checkNotificationPermission()
  }

  override fun showExpirationNotification(expiringProducts: List<ProductWithInstances>) {
    if (expiringProducts.isEmpty()) {
      return
    }

    val notificationManager = context.getSystemService<NotificationManager>()

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

    notificationManager?.notify(NOTIFICATION_ID, notification)
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

  private lateinit var permissionsLauncher: ActivityResultLauncher<String>

  @Suppress("ModifierRequired")
  @Composable
  override fun registerContract() {
    permissionsLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { granted ->
      if (granted) {
        setNotificationsEnabled(true)
      }
    }
  }

  override fun launch() {
    permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
  }

  companion object {
    private const val NOTIFICATION_ID = 1001
  }
}
