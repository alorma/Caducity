package com.alorma.caducity.data.datasource

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.datetime.LocalTime

/**
 * Settings-backed implementation of NotificationConfigDataSource.
 * Persists notification preferences in shared preferences via multiplatform-settings.
 * The notification time is stored as seconds from midnight (single Int).
 * Default notification time is [NotificationConfigDataSource.DEFAULT_TIME] (noon).
 */
class SettingsNotificationConfigDataSource(
  private val settings: Settings,
) : NotificationConfigDataSource {

  override suspend fun getNotificationTime(): LocalTime {
    val secondsFromMidnight = settings.getInt(
      key = NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS,
      defaultValue = NotificationConfigDataSource.DEFAULT_TIME.toSecondOfDay(),
    )
    return LocalTime.fromSecondOfDay(secondsFromMidnight)
  }

  override suspend fun setNotificationTime(time: LocalTime) {
    settings[NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS] = time.toSecondOfDay()
  }

  override suspend fun isNotificationsEnabled(): Boolean {
    return settings.getBoolean(NotificationsEnabledKey, false)
  }

  companion object {
    private const val NotificationsEnabledKey = "notifications_enabled_key"
  }
}
