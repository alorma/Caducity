package com.alorma.caducity.data.datasource

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.datetime.LocalTime

/**
 * Settings-backed implementation of NotificationConfigDataSource.
 * Persists notification preferences in shared preferences via multiplatform-settings.
 * Default notification time is noon (12:00) as defined in [NotificationConfigDataSource.DEFAULT_HOUR].
 */
class SettingsNotificationConfigDataSource(
  private val settings: Settings,
) : NotificationConfigDataSource {

  override suspend fun getNotificationTime(): LocalTime {
    val hour = settings.getInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_HOUR, NotificationConfigDataSource.DEFAULT_HOUR)
    val minute = settings.getInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_MINUTE, NotificationConfigDataSource.DEFAULT_MINUTE)
    return LocalTime(hour, minute)
  }

  override suspend fun setNotificationTime(time: LocalTime) {
    settings[NotificationConfigDataSource.PREF_NOTIFICATION_TIME_HOUR] = time.hour
    settings[NotificationConfigDataSource.PREF_NOTIFICATION_TIME_MINUTE] = time.minute
  }

  override suspend fun isNotificationsEnabled(): Boolean {
    return settings.getBoolean(NotificationsEnabledKey, false)
  }

  companion object {
    private const val NotificationsEnabledKey = "notifications_enabled_key"
  }
}
