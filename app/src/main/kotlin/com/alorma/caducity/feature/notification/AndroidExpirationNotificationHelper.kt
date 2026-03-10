package com.alorma.caducity.feature.notification

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
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import com.alorma.caducity.MainActivity
import com.alorma.caducity.R
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.LocalTime

/**
 * Android implementation of ExpirationNotificationHelper.
 * Builds and shows notifications using Android's notification system.
 */
class AndroidExpirationNotificationHelper(
  private val context: Context,
  private val settings: Settings,
  private val workScheduler: ExpirationWorkScheduler,
  private val stringProvider: StringProvider,
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

  private val notificationTime: MutableState<LocalTime> =
    mutableStateOf(readNotificationTime())

  // Cache large icon bitmap to avoid repeated conversions
  private val largeIconBitmap by lazy {
    ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)?.toBitmap()
  }

  private fun checkNotificationPermission(): Boolean =
    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
      PackageManager.PERMISSION_GRANTED

  private fun readNotificationTime(): LocalTime {
    val secondsFromMidnight =
      settings.getInt(
        key = NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS,
        defaultValue = NotificationConfigDataSource.DEFAULT_TIME.toSecondOfDay(),
      )
    return LocalTime.fromSecondOfDay(secondsFromMidnight)
  }

  override val result: MutableSharedFlow<Any> = MutableSharedFlow()
  private lateinit var permissionsLauncher: ActivityResultLauncher<String>

  @Suppress("ModifierRequired")
  @Composable
  override fun registerContracts() {
    permissionsLauncher =
      rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
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

  override fun hasNotificationPermission(): Boolean = checkNotificationPermission()

  override fun showExpiringSoonNotification(product: NotificationProduct) {
    if (!areNotificationsEnabled().value || !areExpiringSoonNotificationsEnabled().value) return
    showProductNotification(
      product = product,
      channelId = NotificationChannelManager.CHANNEL_ID_EXPIRING_SOON,
      priority = NotificationCompat.PRIORITY_DEFAULT,
    )
  }

  override fun showExpiredNotification(product: NotificationProduct) {
    if (!areNotificationsEnabled().value || !areExpiredNotificationsEnabled().value) return
    showProductNotification(
      product = product,
      channelId = NotificationChannelManager.CHANNEL_ID_EXPIRED,
      priority = NotificationCompat.PRIORITY_HIGH,
    )
  }

  private fun showProductNotification(
    product: NotificationProduct,
    channelId: String,
    priority: Int,
  ) {
    NotificationChannelManager.createNotificationChannels(context)
    val notificationManager = context.getSystemService<NotificationManager>()

    val intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(
          MainActivity.EXTRA_DEEP_LINK_ACTION,
          DeepLinkAction.OpenProduct(categoryId = product.categoryId, productId = product.productId),
        )
      }
    val pendingIntent =
      PendingIntent.getActivity(
        context,
        product.notificationId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val itemLines = buildItemLines(product.items)
    val builder =
      NotificationCompat
        .Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .also { b -> largeIconBitmap?.let { b.setLargeIcon(it) } }
        .setContentTitle(product.title)
        .setContentText(itemLines.firstOrNull() ?: "")
        .setPriority(priority)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setGroup(product.groupKey)

    if (itemLines.size > 1) {
      val style = NotificationCompat.InboxStyle().setBigContentTitle(product.title)
      itemLines.forEach { style.addLine(it) }
      builder.setStyle(style)
    }

    notificationManager?.notify(product.notificationId, builder.build())
  }

  override fun showExpiringSoonGroupSummary(summary: NotificationGroupSummary) {
    if (!areNotificationsEnabled().value || !areExpiringSoonNotificationsEnabled().value) return
    showGroupSummaryNotification(
      summary = summary,
      channelId = NotificationChannelManager.CHANNEL_ID_EXPIRING_SOON,
      priority = NotificationCompat.PRIORITY_DEFAULT,
    )
  }

  override fun showExpiredGroupSummary(summary: NotificationGroupSummary) {
    if (!areNotificationsEnabled().value || !areExpiredNotificationsEnabled().value) return
    showGroupSummaryNotification(
      summary = summary,
      channelId = NotificationChannelManager.CHANNEL_ID_EXPIRED,
      priority = NotificationCompat.PRIORITY_HIGH,
    )
  }

  private fun showGroupSummaryNotification(
    summary: NotificationGroupSummary,
    channelId: String,
    priority: Int,
  ) {
    NotificationChannelManager.createNotificationChannels(context)
    val notificationManager = context.getSystemService<NotificationManager>()

    val intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(
          MainActivity.EXTRA_DEEP_LINK_ACTION,
          DeepLinkAction.OpenCategory(categoryId = summary.categoryId),
        )
      }
    val pendingIntent =
      PendingIntent.getActivity(
        context,
        summary.notificationId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val builder =
      NotificationCompat
        .Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .also { b -> largeIconBitmap?.let { b.setLargeIcon(it) } }
        .setContentTitle(summary.categoryName)
        .setContentText(stringProvider.getString(R.string.notification_items_count, summary.count))
        .setPriority(priority)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setGroup(summary.groupKey)
        .setGroupSummary(true)

    notificationManager?.notify(summary.notificationId, builder.build())
  }

  private fun buildItemLines(items: List<NotificationItem>): List<String> {
    val named = items.mapNotNull { it.identifier?.takeIf { id -> id.isNotBlank() } }
    return if (named.isNotEmpty()) {
      named
    } else {
      listOf(stringProvider.getString(R.string.notification_items_count, items.size))
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

  override fun getNotificationTime(): MutableState<LocalTime> = notificationTime

  override fun setNotificationTime(time: LocalTime) {
    settings[NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS] = time.toSecondOfDay()
    notificationTime.value = time
    workScheduler.rescheduleExpirationCheck(time)
  }

  companion object {
    private const val NotificationsEnabledKey = "notifications_enabled_key"
    private const val ExpiredNotificationsEnabledKey = "notifications_expired_enabled_key"
    private const val ExpiringSoonNotificationsEnabledKey = "notifications_expiring_soon_enabled_key"
  }
}
