package com.alorma.caducity.config.remoteconfig

/**
 * Interface for Firebase Remote Config provider.
 * Allows accessing remote configuration values with type-safe getters.
 */
interface RemoteConfigProvider {
  /**
   * Fetches the latest config values from Firebase Remote Config.
   * @return Result indicating success or failure of the fetch operation.
   */
  suspend fun fetch(): Result<Unit>

  /**
   * Activates the fetched config values.
   * @return Result indicating success or failure of the activation.
   */
  suspend fun activate(): Result<Unit>

  /**
   * Fetches and activates the latest config values in one operation.
   * @return Result indicating success or failure.
   */
  suspend fun fetchAndActivate(): Result<Boolean>

  /**
   * Gets a String value from Remote Config.
   * @param key The config key.
   * @return The string value, or empty string if not found.
   */
  fun getString(key: String): String

  /**
   * Gets a Boolean value from Remote Config.
   * @param key The config key.
   * @return The boolean value, or false if not found.
   */
  fun getBoolean(key: String): Boolean

  /**
   * Gets a Long value from Remote Config.
   * @param key The config key.
   * @return The long value, or 0 if not found.
   */
  fun getLong(key: String): Long

  /**
   * Gets a Double value from Remote Config.
   * @param key The config key.
   * @return The double value, or 0.0 if not found.
   */
  fun getDouble(key: String): Double

  /**
   * Sets default config values.
   * @param defaults Map of default key-value pairs.
   */
  fun setDefaults(defaults: Map<String, Any>)
}
