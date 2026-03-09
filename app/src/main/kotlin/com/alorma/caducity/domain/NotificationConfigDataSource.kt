package com.alorma.caducity.domain

import kotlinx.datetime.LocalTime

/**
 * Data source for notification configuration settings.
 * Provides access to notification preferences like notification time and enabled state.
 */
interface NotificationConfigDataSource {
  /**
   * Returns the time of day when notifications should be scheduled.
   * Defaults to [Defaults.HOUR]:[Defaults.MINUTE] (noon) when not configured.
   */
  suspend fun getNotificationTime(): LocalTime

  /**
   * Persists the time of day when notifications should be scheduled.
   */
  suspend fun setNotificationTime(time: LocalTime)

  /**
   * Returns whether notifications are enabled.
   */
  suspend fun isNotificationsEnabled(): Boolean

  companion object {
    /** SharedPreferences key for the stored notification hour. */
    const val PREF_NOTIFICATION_TIME_HOUR = "notification_time_hour"
    /** SharedPreferences key for the stored notification minute. */
    const val PREF_NOTIFICATION_TIME_MINUTE = "notification_time_minute"

    /** Default notification hour (noon). */
    const val DEFAULT_HOUR = 12
    /** Default notification minute (noon). */
    const val DEFAULT_MINUTE = 0
  }
}
