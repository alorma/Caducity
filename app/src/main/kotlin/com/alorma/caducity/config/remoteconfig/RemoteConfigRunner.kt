package com.alorma.caducity.config.remoteconfig

/**
 * Abstract class for Remote Config implementation.
 * Provides the infrastructure for fetching and accessing remote configuration values.
 * 
 * Similar to FireAndForgetRunner, this class handles the low-level config operations
 * while individual RemoteConfig instances define specific configuration parameters.
 */
abstract class RemoteConfigRunner {
  
  /**
   * Checks if a specific config is enabled.
   * @param remoteConfig The config to check.
   * @return True if enabled, false otherwise.
   */
  fun isEnabled(remoteConfig: RemoteConfig): Boolean {
    return getBoolean(remoteConfig.key, remoteConfig.defaultValue as? Boolean ?: false)
  }
  
  /**
   * Gets a string value from remote config.
   * @param remoteConfig The config to get.
   * @return The string value or default.
   */
  fun getString(remoteConfig: RemoteConfig): String {
    return getString(remoteConfig.key, remoteConfig.defaultValue as? String ?: "")
  }
  
  /**
   * Gets a long value from remote config.
   * @param remoteConfig The config to get.
   * @return The long value or default.
   */
  fun getLong(remoteConfig: RemoteConfig): Long {
    return getLong(remoteConfig.key, remoteConfig.defaultValue as? Long ?: 0L)
  }
  
  /**
   * Gets a double value from remote config.
   * @param remoteConfig The config to get.
   * @return The double value or default.
   */
  fun getDouble(remoteConfig: RemoteConfig): Double {
    return getDouble(remoteConfig.key, remoteConfig.defaultValue as? Double ?: 0.0)
  }
  
  /**
   * Fetches and activates the latest config values.
   * @return Result indicating success or failure.
   */
  abstract suspend fun fetchAndActivate(): Result<Boolean>
  
  /**
   * Gets a boolean value from the config store.
   */
  protected abstract fun getBoolean(key: String, defaultValue: Boolean): Boolean
  
  /**
   * Gets a string value from the config store.
   */
  protected abstract fun getString(key: String, defaultValue: String): String
  
  /**
   * Gets a long value from the config store.
   */
  protected abstract fun getLong(key: String, defaultValue: Long): Long
  
  /**
   * Gets a double value from the config store.
   */
  protected abstract fun getDouble(key: String, defaultValue: Double): Double
}
