package com.alorma.caducity.domain

import kotlinx.datetime.LocalTime

/**
 * Data source for notification configuration settings.
 * Provides access to notification preferences like notification time and enabled state.
 */
interface NotificationConfigDataSource {
  /**
   * Returns the time of day when notifications should be scheduled.
   * Defaults to [DEFAULT_TIME] (noon) when not configured.
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
    /** SharedPreferences key for the stored notification time (seconds from midnight). */
    const val PREF_NOTIFICATION_TIME_SECONDS = "notification_time_seconds"

    /** Default notification time: noon. */
    val DEFAULT_TIME = LocalTime(12, 0)
  }
}
