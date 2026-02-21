package com.alorma.caducity.feature.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import com.alorma.caducity.MainActivity
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.CategoryWithItems
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Android implementation of ExpirationNotificationHelper.
 * Builds and shows notifications using Android's notification system.
 */
class AndroidExpirationNotificationHelper(
  private val context: Context,
  private val settings: Settings,
) : ExpirationNotificationHelper {

  private val notifications: MutableState<Boolean> =
    mutableStateOf(
      // Initialize based on both permission status and saved preference
      settings.getBoolean(NotificationsEnabledKey, false) && checkNotificationPermission(),
    )

  private val expiredNotifications: MutableState<Boolean> =
    mutableStateOf(settings.getBoolean(ExpiredNotificationsEnabledKey, true))

  private val expiringSoonNotifications: MutableState<Boolean> =
    mutableStateOf(settings.getBoolean(ExpiringSoonNotificationsEnabledKey, true))

  // Cache large icon bitmap to avoid repeated conversions
  private val largeIconBitmap by lazy {
    ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)?.toBitmap()
  }

  private fun checkNotificationPermission(): Boolean {
    return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
  }

  override val result: MutableSharedFlow<Any> = MutableSharedFlow()
  private lateinit var permissionsLauncher: ActivityResultLauncher<String>

  @Suppress("ModifierRequired")
  @Composable
  override fun registerContracts() {
    permissionsLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { granted ->
      if (granted) {
        setNotificationsEnabled(true)
      }
    }
  }


  override fun areNotificationsEnabled(): MutableState<Boolean> {
    // Sync state with permission status
    // If permission was revoked externally, update state to false
    if (notifications.value && !checkNotificationPermission()) {
      notifications.value = false
      settings[NotificationsEnabledKey] = false
    }
    return notifications
  }

  override fun setNotificationsEnabled(enabled: Boolean) {
    if (enabled) {
      // Can only enable if permission is granted
      val hasPermission = checkNotificationPermission()
      if (hasPermission) {
        settings[NotificationsEnabledKey] = true
        notifications.value = true
      }
    } else {
      settings[NotificationsEnabledKey] = false
      notifications.value = false
    }
  }

  override fun hasNotificationPermission(): Boolean {
    return checkNotificationPermission()
  }

  override fun showExpirationNotification(expiringProducts: List<CategoryWithItems>) {
    if (!areNotificationsEnabled().value) {
      return
    }

    if (expiringProducts.isEmpty()) {
      return
    }

    val notificationManager = context.getSystemService<NotificationManager>()

    // Create intent to open app with filtered view
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
        .setSmallIcon(R.drawable.ic_notification)
        .also { builder ->
          largeIconBitmap?.let { builder.setLargeIcon(it) }
        }
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
      "Category expiring soon"
    } else {
      "$count categories expiring soon"
    }
  }

  private fun buildNotificationText(categories: List<CategoryWithItems>): String {
    return if (categories.size == 1) {
      categories.first().category.name
    } else {
      "${categories.first().category.name} and ${categories.size - 1} more"
    }
  }

  override fun launch() {
    permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
  }

  override fun changeState(enabled: Boolean) {
    if (enabled) {
      // Check if we need to request permission
      if (!hasNotificationPermission()) {
        launch()
      } else {
        setNotificationsEnabled(true)
      }
    } else {
      setNotificationsEnabled(false)
    }
  }

  override fun areExpiredNotificationsEnabled(): MutableState<Boolean> = expiredNotifications

  override fun setExpiredNotificationsEnabled(enabled: Boolean) {
    settings[ExpiredNotificationsEnabledKey] = enabled
    expiredNotifications.value = enabled
  }

  override fun areExpiringSoonNotificationsEnabled(): MutableState<Boolean> = expiringSoonNotifications

  override fun setExpiringSoonNotificationsEnabled(enabled: Boolean) {
    settings[ExpiringSoonNotificationsEnabledKey] = enabled
    expiringSoonNotifications.value = enabled
  }

  companion object {
    private const val NOTIFICATION_ID = 1001
    private const val NotificationsEnabledKey = "notifications_enabled_key"
    private const val ExpiredNotificationsEnabledKey = "notifications_expired_enabled_key"
    private const val ExpiringSoonNotificationsEnabledKey = "notifications_expiring_soon_enabled_key"
  }
}
