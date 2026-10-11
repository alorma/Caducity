package com.alorma.caducity.config.preferences

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.russhwolf.settings.MapSettings
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse

class PreferencesMigratorStep2Test {
  private val settings = MapSettings()
  private val enabled = NotificationConfigDataSource.PREF_DAILY_REMINDER_ENABLED

  private fun migrate() = PreferencesMigrator(settings).migrate()

  @Test
  fun `the reminder stays on when notifications and at least one type were on`() {
    settings.putBoolean("notifications_enabled_key", true)
    settings.putBoolean("notifications_expired_enabled_key", false)
    migrate()
    expectThat(settings.getBoolean(enabled, false)).isEqualTo(true)
  }

  @Test
  fun `the reminder is off when notifications were off or both types were off`() {
    settings.putBoolean("notifications_enabled_key", false)
    migrate()
    expectThat(settings.getBoolean(enabled, true)).isEqualTo(false)

    val both = MapSettings()
    both.putBoolean("notifications_enabled_key", true)
    both.putBoolean("notifications_expired_enabled_key", false)
    both.putBoolean("notifications_expiring_soon_enabled_key", false)
    PreferencesMigrator(both).migrate()
    expectThat(both.getBoolean(enabled, true)).isEqualTo(false)
  }

  @Test
  fun `the old keys are removed, the version is 2 and the time is kept`() {
    settings.putBoolean("fire-and-forget-user_onboarding", false)
    settings.putBoolean("notifications_enabled_key", true)
    settings.putBoolean("notifications_expired_enabled_key", true)
    settings.putBoolean("notifications_expiring_soon_enabled_key", true)
    settings.putInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS, 8 * 3600)
    migrate()
    migrate()

    expectThat(settings.hasKey("notifications_enabled_key")).isFalse()
    expectThat(settings.hasKey("notifications_expired_enabled_key")).isFalse()
    expectThat(settings.hasKey("notifications_expiring_soon_enabled_key")).isFalse()
    expectThat(settings.getInt("prefs_version", 0)).isEqualTo(2)
    expectThat(settings.getInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS, -1)).isEqualTo(8 * 3600)
  }

  @Test
  fun `an already stored reminder choice is not overwritten`() {
    settings.putBoolean(enabled, true)
    settings.putBoolean("notifications_enabled_key", false)
    migrate()
    expectThat(settings.getBoolean(enabled, false)).isEqualTo(true)
  }
}
