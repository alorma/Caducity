package com.alorma.caducity.config.remoteconfig

import com.russhwolf.settings.Settings

/**
 * Debug implementation of RemoteConfigRunner.
 *
 * This runner allows overriding remote config values for debugging purposes.
 * It checks for saved override values in settings first, and falls back to
 * the default RemoteConfigRunner (Firebase) if no override exists.
 *
 * Similar to ThemePreferencesImpl, it uses Settings for persistence.
 */
class DebugRemoteConfigRunner(
  private val settings: Settings,
  private val defaultRunner: RemoteConfigRunner,
) : RemoteConfigRunner() {
  companion object {
    private const val KEY_PREFIX = "debug_remote_config_"
    private const val KEY_OVERRIDE_PREFIX = "debug_remote_config_override_"
  }

  override suspend fun fetchAndActivate(): Result<Boolean> {
    // Delegate to the default runner for fetching
    return defaultRunner.fetchAndActivate()
  }

  override fun getBoolean(
    key: String,
    defaultValue: Boolean,
  ): Boolean {
    val overrideKey = KEY_OVERRIDE_PREFIX + key
    val hasOverride = settings.getBoolean(overrideKey, false)

    return if (hasOverride) {
      // Use the debug override value
      val valueKey = KEY_PREFIX + key
      settings.getBoolean(valueKey, defaultValue)
    } else {
      // Fallback to default runner (Firebase)
      // Note: We use isEnabled() via an anonymous RemoteConfig because getBoolean() is protected.
      // This is only used in debug builds and the object creation overhead is negligible.
      defaultRunner.isEnabled(object : RemoteConfig(defaultRunner, key, defaultValue) {})
    }
  }

  /**
   * Sets a debug override value for a specific config key.
   * @param key The config key to override.
   * @param value The override value.
   */
  fun setDebugValue(
    key: String,
    value: Boolean,
  ) {
    val valueKey = KEY_PREFIX + key
    val overrideKey = KEY_OVERRIDE_PREFIX + key

    settings.putBoolean(valueKey, value)
    settings.putBoolean(overrideKey, true)
  }

  /**
   * Clears the debug override for a specific config key.
   * After this, the config will use the default runner value.
   * @param key The config key to clear.
   */
  fun clearDebugValue(key: String) {
    val overrideKey = KEY_OVERRIDE_PREFIX + key
    settings.remove(overrideKey)
  }

  /**
   * Checks if a config key has a debug override.
   * @param key The config key to check.
   * @return True if there's a debug override, false otherwise.
   */
  fun hasDebugOverride(key: String): Boolean {
    val overrideKey = KEY_OVERRIDE_PREFIX + key
    return settings.getBoolean(overrideKey, false)
  }

  /**
   * Clears all debug overrides.
   */
  fun clearAllDebugValues() {
    settings.keys.forEach { key ->
      if (key.startsWith(KEY_PREFIX) || key.startsWith(KEY_OVERRIDE_PREFIX)) {
        settings.remove(key)
      }
    }
  }
}
