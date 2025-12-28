package com.alorma.caducity.data.datasource

import kotlinx.datetime.LocalTime

/**
 * Data source for notification configuration settings.
 * Provides access to notification preferences like threshold days and notification time.
 */
interface NotificationConfigDataSource {
  /**
   * Returns the number of days before expiration when notifications should be sent.
   * For example, if threshold is 3, notifications are sent when products expire in 3 days or less.
   */
  suspend fun getExpirationThresholdDays(): Int

  /**
   * Returns the time of day when notifications should be scheduled.
   * For example, LocalTime(9, 0) means notifications are sent at 9:00 AM.
   */
  suspend fun getNotificationTime(): LocalTime

  /**
   * Returns whether notifications are enabled.
   */
  suspend fun isNotificationsEnabled(): Boolean
}
