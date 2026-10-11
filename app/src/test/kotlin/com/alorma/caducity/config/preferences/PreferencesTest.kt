package com.alorma.caducity.config.preferences

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class PreferencesTest {
  private val settings = MapSettings()

  // region PreferencesMigrator

  @Test
  fun `step 1 on an existing install sets the threshold, drops removed keys and keeps the 12h reminder`() {
    settings.putBoolean("user_onboarding", false)
    settings.putString("theme_tone", "VIBRANT")
    settings.putString("ai_selected_model", "gemma")
    settings.putLong("ai_download_progress", 5)
    settings.putString("theme_mode", "DARK")
    settings.putBoolean("notifications_enabled_key", true)

    PreferencesMigrator(settings).migrate()

    expectThat(settings.getInt("soon_threshold_days", -1)).isEqualTo(3)
    expectThat(settings.hasKey("theme_tone")).isFalse()
    expectThat(settings.keys.any { it.startsWith("ai_") }).isFalse()
    expectThat(settings.getInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS, -1)).isEqualTo(12 * 3600)
    expectThat(settings.getInt("prefs_version", 0)).isEqualTo(1)
    // Untouched keys.
    expectThat(settings.getString("theme_mode", "")).isEqualTo("DARK")
    expectThat(settings.getBoolean("notifications_enabled_key", false)).isTrue()
    expectThat(settings.getBoolean("user_onboarding", true)).isFalse()
  }

  @Test
  fun `step 1 keeps an explicit reminder time and a fresh install gets the new default`() {
    settings.putBoolean("user_onboarding", false)
    settings.putInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS, 7 * 3600)
    PreferencesMigrator(settings).migrate()
    expectThat(settings.getInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS, -1)).isEqualTo(7 * 3600)

    val fresh = MapSettings()
    PreferencesMigrator(fresh).migrate()
    expectThat(fresh.hasKey(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS)).isFalse()
    expectThat(fresh.getInt("soon_threshold_days", -1)).isEqualTo(3)
  }

  @Test
  fun `migration is idempotent and does not overwrite later changes`() {
    settings.putBoolean("user_onboarding", false)
    val migrator = PreferencesMigrator(settings)
    migrator.migrate()
    settings.putInt("soon_threshold_days", 7)
    settings.putInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS, 8 * 3600)
    settings.putString("ai_selected_model", "again")

    migrator.migrate()
    PreferencesMigrator(settings).migrate()

    expectThat(settings.getInt("soon_threshold_days", -1)).isEqualTo(7)
    expectThat(settings.getInt(NotificationConfigDataSource.PREF_NOTIFICATION_TIME_SECONDS, -1)).isEqualTo(8 * 3600)
    expectThat(settings.hasKey("ai_selected_model")).isTrue()
  }

  @Test
  fun `step 1 keeps an already stored threshold`() {
    settings.putInt("soon_threshold_days", 7)
    PreferencesMigrator(settings).migrate()
    expectThat(settings.getInt("soon_threshold_days", -1)).isEqualTo(7)
  }

  // endregion

  // region SoonThresholdPreferences

  @Test
  fun `threshold defaults to 3, persists and emits changes`() =
    runTest {
      val prefs = SoonThresholdPreferences(settings)
      expectThat(prefs.thresholdDays.first()).isEqualTo(3)

      prefs.setThresholdDays(7)
      expectThat(prefs.thresholdDays.first()).isEqualTo(7)
      expectThat(SoonThresholdPreferences(settings).current()).isEqualTo(7)
    }

  @Test
  fun `threshold only accepts 1, 3 and 7 and ignores corrupt stored values`() {
    val prefs = SoonThresholdPreferences(settings)
    var rejected = false
    try {
      prefs.setThresholdDays(5)
    } catch (_: IllegalArgumentException) {
      rejected = true
    }
    expectThat(rejected).isTrue()
    expectThat(prefs.current()).isEqualTo(3)

    settings.putInt("soon_threshold_days", 42)
    expectThat(SoonThresholdPreferences(settings).current()).isEqualTo(3)
  }

  // endregion

  // region DisclaimerPreferences

  @Test
  fun `disclaimer is absent for existing users and needs acceptance until accepted`() {
    val prefs = DisclaimerPreferences(settings)
    expectThat(prefs.acceptedVersion()).isNull()
    expectThat(prefs.acceptedAt()).isNull()
    expectThat(prefs.needsAcceptance()).isTrue()

    val at = Instant.parse("2026-10-11T10:00:00Z")
    prefs.accept(at)
    expectThat(prefs.acceptedVersion()).isEqualTo(DisclaimerPreferences.CURRENT_VERSION)
    expectThat(prefs.acceptedAt()).isEqualTo(at)
    expectThat(prefs.needsAcceptance()).isFalse()
    expectThat(prefs.needsAcceptance(currentVersion = DisclaimerPreferences.CURRENT_VERSION + 1)).isTrue()
  }

  // endregion
}
