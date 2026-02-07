package com.alorma.caducity.config.remoteconfig

/**
 * Default config values for Firebase Remote Config.
 * Define your config keys and default values here.
 * 
 * Usage example:
 * ```
 * // In ConfigModule:
 * remoteConfigProvider.setDefaults(RemoteConfigDefaults.defaults)
 * 
 * // Access values:
 * val featureEnabled = remoteConfigProvider.getBoolean(RemoteConfigDefaults.Keys.EXAMPLE_FEATURE_ENABLED)
 * ```
 */
object RemoteConfigDefaults {
  
  /**
   * Config keys used in Remote Config.
   * Add new keys here as you create new remote configs.
   */
  object Keys {
    const val EXAMPLE_FEATURE_ENABLED = "example_feature_enabled"
    const val EXAMPLE_MESSAGE = "example_message"
    const val EXAMPLE_NUMBER = "example_number"
  }
  
  /**
   * Default values for all config keys.
   * These will be used until remote values are fetched and activated.
   */
  val defaults: Map<String, Any> = mapOf(
    Keys.EXAMPLE_FEATURE_ENABLED to false,
    Keys.EXAMPLE_MESSAGE to "Hello from Remote Config!",
    Keys.EXAMPLE_NUMBER to 42L,
  )
}
