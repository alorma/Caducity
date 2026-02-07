package com.alorma.caducity.config.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Firebase implementation of RemoteConfigRunner.
 * Wraps Firebase Remote Config SDK and provides access to config values.
 */
class FirebaseRemoteConfigProvider(
  private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
) : RemoteConfigRunner() {

  init {
    // Configure Remote Config settings
    val configSettings = remoteConfigSettings {
      // In debug builds, fetch configs more frequently for testing
      minimumFetchIntervalInSeconds = if (com.alorma.caducity.BuildConfig.DEBUG) {
        60 // 1 minute for debug builds
      } else {
        3600 // 1 hour for release builds
      }
    }
    remoteConfig.setConfigSettingsAsync(configSettings)
  }

  override suspend fun fetchAndActivate(): Result<Boolean> = runCatching {
    val activated = remoteConfig.fetchAndActivate().await()
    Timber.d("Remote Config: Fetch and activate successful. Activated: $activated")
    activated
  }.onFailure { exception ->
    Timber.e(exception, "Remote Config: Fetch and activate failed")
  }

  override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
    return remoteConfig.getBoolean(key)
  }

  override fun getString(key: String, defaultValue: String): String {
    return remoteConfig.getString(key)
  }

  override fun getLong(key: String, defaultValue: Long): Long {
    return remoteConfig.getLong(key)
  }

  override fun getDouble(key: String, defaultValue: Double): Double {
    return remoteConfig.getDouble(key)
  }

  /**
   * Sets default config values for all configs.
   * @param defaults Map of default key-value pairs.
   */
  fun setDefaults(defaults: Map<String, Any>) {
    remoteConfig.setDefaultsAsync(defaults)
    Timber.d("Remote Config: Defaults set with ${defaults.size} values")
  }
}
