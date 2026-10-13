package com.alorma.caducity.feature.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import com.alorma.caducity.MainActivity
import com.alorma.caducity.R
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.ThemePreferences
import com.alorma.caducity.ui.theme.colors.accent
import com.alorma.caducity.ui.theme.colors.supportsDynamicColors
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.LocalTime

/**
 * Android implementation of ExpirationNotificationHelper.
 * Builds and shows the daily reminder using Android's notification system.
 */
class AndroidExpirationNotificationHelper(
  private val context: Context,
  private val settings: Settings,
  private val workScheduler: ExpirationWorkScheduler,
  private val stringProvider: StringProvider,
  private val themePreferences: ThemePreferences,
) : ExpirationNotificationHelper {
  private val notifications: MutableState<Boolean> =
    mutableStateOf(
      // Initialize based on both permission status and saved preference
      settings.getBoolean(NotificationConfigDataSource.PREF_DAILY_REMINDER_ENABLED, false) &&
        checkNotificationPermission(),
    )

  // Cache large icon bitmap to avoid repeated conversions
  private val largeIconBitmap by lazy {
    ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)?.toBitmap()
  }

  private fun checkNotificationPermission(): Boolean = context.hasNotificationPermission()

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
      settings[NotificationConfigDataSource.PREF_DAILY_REMINDER_ENABLED] = false
    }
    return notifications
  }

  override fun setNotificationsEnabled(enabled: Boolean) {
    if (enabled) {
      // Can only enable if permission is granted
      if (checkNotificationPermission()) {
        settings[NotificationConfigDataSource.PREF_DAILY_REMINDER_ENABLED] = true
        notifications.value = true
        workScheduler.scheduleExpirationCheck(readNotificationTime())
      }
    } else {
      settings[NotificationConfigDataSource.PREF_DAILY_REMINDER_ENABLED] = false
      notifications.value = false
      workScheduler.cancelExpirationCheck()
    }
  }

  override fun hasNotificationPermission(): Boolean = checkNotificationPermission()

  override fun showDailyReminder(content: ReminderContent) {
    if (!areNotificationsEnabled().value) return
    NotificationChannelManager.createNotificationChannels(context)
    val notificationManager = context.getSystemService<NotificationManager>()

    val intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(MainActivity.EXTRA_DEEP_LINK_ACTION, DeepLinkAction.OpenHome)
      }
    val pendingIntent =
      PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val body = buildBody(content)
    val notification =
      NotificationCompat
        .Builder(context, NotificationChannelManager.CHANNEL_ID_DAILY_REMINDER)
        .setSmallIcon(R.drawable.ic_notification)
        .setColor(accentColor().toArgb())
        .also { b -> largeIconBitmap?.let { b.setLargeIcon(it) } }
        .setContentTitle(stringProvider.getQuantityString(R.plurals.notification_title, content.count, content.count))
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    notificationManager?.notify(NOTIFICATION_ID, notification)
  }

  /** `primary` (clar) de la paleta triada; Dinàmic: el color del sistema, fora de Compose. */
  private fun accentColor(): Color {
    val palette = themePreferences.colorPalette.value
    return if (palette == ColorPalette.DYNAMIC && supportsDynamicColors()) {
      dynamicLightColorScheme(context).primary
    } else {
      palette.accent()
    }
  }

  private fun buildBody(content: ReminderContent): String =
    content.highlights.joinToString(separator = ", ") { highlight ->
      when (highlight.daysLeft) {
        0 -> stringProvider.getString(R.string.notification_highlight_today, highlight.name)
        1 -> stringProvider.getString(R.string.notification_highlight_tomorrow, highlight.name)
        else -> stringProvider.getString(R.string.notification_highlight_days, highlight.name, highlight.daysLeft)
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

  companion object {
    private const val NOTIFICATION_ID = 1001
  }
}
