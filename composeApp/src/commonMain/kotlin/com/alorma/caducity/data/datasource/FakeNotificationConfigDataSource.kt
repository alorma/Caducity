package com.alorma.caducity.data.datasource

import com.alorma.caducity.domain.NotificationConfigDataSource
import kotlinx.datetime.LocalTime

/**
 * Fake implementation of NotificationConfigDataSource with hardcoded values.
 * This will be replaced with a proper Room-based implementation when adding
 * user-configurable notification settings.
 */
class FakeNotificationConfigDataSource : NotificationConfigDataSource {
  /**
   * Hardcoded to 9:00 AM.
   * In future, this will be configurable by the user via settings.
   */
  override suspend fun getNotificationTime(): LocalTime = LocalTime(9, 0)

  /**
   * Hardcoded to enabled.
   * In future, this will be configurable by the user via settings.
   */
  override suspend fun isNotificationsEnabled(): Boolean = true
}