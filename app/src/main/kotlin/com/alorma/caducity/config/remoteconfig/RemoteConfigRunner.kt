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
    return getBoolean(remoteConfig.key, remoteConfig.defaultValue)
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
}
