package com.alorma.caducity.domain

import kotlinx.datetime.LocalTime

/**
 * Data source for notification configuration settings.
 * Provides access to notification preferences like notification time and enabled state.
 *
 * Note: Expiration threshold is handled by ExpirationThresholds interface.
 */
interface NotificationConfigDataSource {
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
