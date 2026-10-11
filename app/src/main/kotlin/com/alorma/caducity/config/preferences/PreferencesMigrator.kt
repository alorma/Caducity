package com.alorma.caducity.config.preferences

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.russhwolf.settings.Settings

/**
 * Versioned, idempotent migration of the stored preferences (key `prefs_version`).
 *
 * - Step 1 (phase 2): `soon_threshold_days = 3` (equivalent to the old fixed behavior), drops the removed
 *   `theme_tone` and `ai_*` keys, and keeps the reminder time for existing users (the default moved from
 *   12:00 to 09:00, so existing installs get 12:00 written explicitly).
 * - Step 2 (phase 8, not yet applied): derive the daily reminder from the old notification keys.
 */
class PreferencesMigrator(
  private val settings: Settings,
) {
  fun migrate() {
    val version = settings.getInt(KEY_VERSION, 0)
    if (version < 1) migrateToV1()
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

  companion object {
    const val KEY_VERSION = "prefs_version"
    private const val KEY_ONBOARDING = "user_onboarding"
    private const val KEY_THEME_TONE = "theme_tone"
    private const val AI_PREFIX = "ai_"
    private val LEGACY_DEFAULT_REMINDER = kotlinx.datetime.LocalTime(12, 0)
  }
}
