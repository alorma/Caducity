package com.alorma.caducity.config.preferences

import com.russhwolf.settings.Settings
import kotlin.time.Instant

/** Which version of the legal notice the user accepted, and when. Absent for users who never accepted it. */
class DisclaimerPreferences(
  private val settings: Settings,
) {
  fun acceptedVersion(): Int? = settings.getIntOrNull(KEY_VERSION)

  fun acceptedAt(): Instant? = settings.getLongOrNull(KEY_AT)?.let { Instant.fromEpochMilliseconds(it) }

  /** True when the user never accepted the notice or accepted an older version (only that step is shown again). */
  fun needsAcceptance(currentVersion: Int = CURRENT_VERSION): Boolean = (acceptedVersion() ?: 0) < currentVersion

  fun accept(
    at: Instant,
    version: Int = CURRENT_VERSION,
  ) {
    settings.putInt(KEY_VERSION, version)
    settings.putLong(KEY_AT, at.toEpochMilliseconds())
  }

  companion object {
    const val KEY_VERSION = "disclaimer_accepted_version"
    const val KEY_AT = "disclaimer_accepted_at"
    const val CURRENT_VERSION = 1
  }
}
