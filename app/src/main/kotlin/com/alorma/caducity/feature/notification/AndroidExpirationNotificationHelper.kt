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
import androidx.core.content.getSystemService
import com.alorma.caducity.MainActivity
import com.alorma.caducity.R
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.ThemePreferences
import com.alorma.caducity.ui.theme.colors.accent
import com.alorma.caducity.ui.theme.colors.supportsDynamicColors
import com.alorma.caducity.ui.unit.countPlural
import com.alorma.caducity.ui.unit.formatAmount
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import java.util.Locale
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

  override fun showDailyReminders(reminders: DailyReminders) {
    if (!areNotificationsEnabled().value) return
    NotificationChannelManager.createNotificationChannels(context)
    val notificationManager = context.getSystemService<NotificationManager>() ?: return

    // Fixed ids: today's reminder replaces yesterday's, and a stale one is removed when there is nothing to say.
    show(notificationManager, NOTIFICATION_ID_SOON, reminders.soon)
    show(notificationManager, NOTIFICATION_ID_TODAY, reminders.today)
  }

  private fun show(
    notificationManager: NotificationManager,
    id: Int,
    content: ReminderContent?,
  ) {
    if (content == null) {
      notificationManager.cancel(id)
      return
    }

    val intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(MainActivity.EXTRA_DEEP_LINK_ACTION, DeepLinkAction.OpenHome)
      }
    val pendingIntent =
      PendingIntent.getActivity(
        context,
        id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val body = buildBody(content)
    val builder =
      NotificationCompat
        .Builder(context, NotificationChannelManager.CHANNEL_ID_DAILY_REMINDER)
        .setSmallIcon(R.drawable.ic_notification)
        .setColor(accentColor().toArgb())
        .setContentTitle(buildTitle(content))
        .setContentText(body)
        .setStyle(buildStyle(content, body))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    notificationManager.notify(id, builder.build())
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

  private fun dayLabel(product: ReminderProduct): String =
    when {
      product.daysLeft <= 0 -> stringProvider.getString(R.string.notification_day_today)
      product.daysLeft == 1 -> stringProvider.getString(R.string.notification_day_tomorrow)
      else -> ReminderDayLabel.format(product.expiration, product.daysLeft, Locale.getDefault())
    }

  private fun buildTitle(content: ReminderContent): String {
    val first = content.products.first()
    return when {
      content.products.size == 1 ->
        stringProvider.getString(R.string.notification_title_single, first.name, dayLabel(first))
      content.sameDay ->
        stringProvider.getQuantityString(
          R.plurals.notification_title_same_day,
          content.products.size,
          content.products.size,
          dayLabel(first),
        )
      else ->
        stringProvider.getQuantityString(
          R.plurals.notification_title_soon,
          content.products.size,
          content.products.size,
        )
    }
  }

  /** «3 units», or the amount of a measured product («500 g»); nothing for a single unit. */
  private fun quantityText(
    product: ReminderProduct,
    short: Boolean,
  ): String? =
    if (product.amount != null && product.unitType.parent == UnitParent.MEASURED) {
      formatAmount(product.unitType, product.amount)
    } else {
      product.units.takeIf { it > 1 }?.let {
        stringProvider.getQuantityString(product.unitType.notificationPlural(short), it, it)
      }
    }

  private fun buildBody(content: ReminderContent): String {
    val shown = content.shown
    return when {
      content.products.size == 1 -> {
        val product = shown.first()
        listOfNotNull(
          quantityText(product, short = false),
          product.categoryName,
        ).joinToString(separator = SEPARATOR)
      }
      content.sameDay -> joinNames(shown.map { it.name }, content.hiddenCount)
      else -> {
        val items = shown.map { stringProvider.getString(R.string.notification_product_day, it.name, dayLabel(it)) }
        val joined = items.joinToString(separator = SEPARATOR)
        if (content.hiddenCount > 0) {
          stringProvider.getString(R.string.notification_list_more, joined, content.hiddenCount)
        } else {
          joined
        }
      }
    }
  }

  /** «A, B i C»; with hidden products «A, B, C, D, E i 3 més». */
  private fun joinNames(
    names: List<String>,
    hiddenCount: Int,
  ): String =
    when {
      hiddenCount > 0 ->
        stringProvider.getString(R.string.notification_list_more, names.joinToString(separator = ", "), hiddenCount)
      names.size == 1 -> names.first()
      else ->
        stringProvider.getString(
          R.string.notification_list_last,
          names.dropLast(1).joinToString(separator = ", "),
          names.last(),
        )
    }

  private fun buildStyle(
    content: ReminderContent,
    body: String,
  ): NotificationCompat.Style {
    if (content.products.size == 1) return NotificationCompat.BigTextStyle().bigText(body)
    return NotificationCompat.InboxStyle().also { style ->
      content.shown.forEach { product ->
        val detail =
          listOfNotNull(
            dayLabel(product),
            quantityText(product, short = true),
          ).joinToString(separator = SEPARATOR)
        style.addLine(stringProvider.getString(R.string.notification_inbox_line, product.name, detail))
      }
      if (content.hiddenCount > 0) {
        style.addLine(stringProvider.getString(R.string.notification_more, content.hiddenCount))
      }
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
    private const val NOTIFICATION_ID_SOON = 1001
    private const val NOTIFICATION_ID_TODAY = 1002
    private const val SEPARATOR = " · "
  }
}

/** Units keep their own wording («3 units», «3 pcs»); the other types are short enough to say as they are. */
private fun UnitType.notificationPlural(short: Boolean): Int =
  if (this == UnitType.UNITS) {
    if (short) R.plurals.notification_units_short else R.plurals.notification_units
  } else {
    countPlural
  }
