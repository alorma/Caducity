package com.alorma.caducity.config.preferences

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.russhwolf.settings.Settings

/**
 * Versioned, idempotent migration of the stored preferences (key `prefs_version`).
 *
 * - Step 1 (phase 2): `soon_threshold_days = 3` (equivalent to the old fixed behavior), drops the removed
 *   `theme_tone` and `ai_*` keys, and keeps the reminder time for existing users (the default moved from
 *   12:00 to 09:00, so existing installs get 12:00 written explicitly).
 * - Step 2 (phase 8): derives `daily_reminder_enabled` from the old notification keys (the reminder stays on only if
 *   notifications were on and at least one of the expired/soon types was) and drops the old keys.
 */
class PreferencesMigrator(
  private val settings: Settings,
) {
  fun migrate() {
    val version = settings.getInt(KEY_VERSION, 0)
    if (version < 1) migrateToV1()
    if (version < 2) migrateToV2()
  }

  private fun migrateToV1() {
    val existingInstall = settings.hasKey(KEY_ONBOARDING)

    if (!settings.hasKey(SoonThresholdPreferences.KEY)) {
      settings.putInt(SoonThresholdPreferences.KEY, SoonThresholdPreferences.DEFAULT_DAYS)
    }

    settings.remove(KEY_THEME_TONE)
    settings.keys.filter { it.startsWith(AI_PREFIX) }.forEach(settings::remove)

    val timeKey = NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS
    if (existingInstall && !settings.hasKey(timeKey)) {
      settings.putInt(timeKey, LEGACY_DEFAULT_REMINDER.toSecondOfDay())
    }

    settings.putInt(KEY_VERSION, 1)
  }

  private fun migrateToV2() {
    if (!settings.hasKey(NotificationConfigDataSource.PREF_DAILY_REMINDER_ENABLED)) {
      val notifications = settings.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
      val expired = settings.getBoolean(KEY_EXPIRED_ENABLED, true)
      val soon = settings.getBoolean(KEY_SOON_ENABLED, true)
      settings.putBoolean(NotificationConfigDataSource.PREF_DAILY_REMINDER_ENABLED, notifications && (expired || soon))
    }

    settings.remove(KEY_NOTIFICATIONS_ENABLED)
    settings.remove(KEY_EXPIRED_ENABLED)
    settings.remove(KEY_SOON_ENABLED)

    settings.putInt(KEY_VERSION, 2)
  }

  companion object {
    const val KEY_VERSION = "prefs_version"

    // FireAndForget stores its flags with this prefix; the flag exists only on installs that ran the onboarding.
    private const val KEY_ONBOARDING = "fire-and-forget-user_onboarding"
    private const val KEY_THEME_TONE = "theme_tone"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled_key"
    private const val KEY_EXPIRED_ENABLED = "notifications_expired_enabled_key"
    private const val KEY_SOON_ENABLED = "notifications_expiring_soon_enabled_key"
    private const val AI_PREFIX = "ai_"
    private val LEGACY_DEFAULT_REMINDER = kotlinx.datetime.LocalTime(12, 0)
  }
}
