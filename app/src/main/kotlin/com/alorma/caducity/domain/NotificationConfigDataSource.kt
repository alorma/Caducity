package com.alorma.caducity.domain

import kotlinx.datetime.LocalTime

/**
 * Data source for notification configuration settings.
 * Provides access to notification preferences like notification time and enabled state.
 */
interface NotificationConfigDataSource {
  /**
   * Returns the time of day when notifications should be scheduled.
   * Defaults to [DEFAULT_TIME] (09:00) when not configured.
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

  /** Enables or disables the daily reminder (kept in sync with the notification permission). */
  suspend fun setNotificationsEnabled(enabled: Boolean)

  companion object {
    /** SharedPreferences key for the stored notification time (seconds from midnight). */
    const val PREF_NOTIFICATION_TIME_SECONDS = "notification_time_seconds"

    /** Default notification time: 09:00. Existing installs keep 12:00 through the preferences migrator. */
    val DEFAULT_TIME = LocalTime(9, 0)
  }
}
