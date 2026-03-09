package com.alorma.caducity.data.datasource

import com.alorma.caducity.domain.NotificationConfigDataSource
import kotlinx.datetime.LocalTime

/**
 * Fake implementation of NotificationConfigDataSource with hardcoded values.
 * Only used for testing. Production code uses SettingsNotificationConfigDataSource.
 */
class FakeNotificationConfigDataSource : NotificationConfigDataSource {
  override suspend fun getNotificationTime(): LocalTime = NotificationConfigDataSource.DEFAULT_TIME

  override suspend fun setNotificationTime(time: LocalTime) {
    // no-op in fake implementation
  }

  override suspend fun isNotificationsEnabled(): Boolean = true
}
