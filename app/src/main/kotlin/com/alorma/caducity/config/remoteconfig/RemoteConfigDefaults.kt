package com.alorma.caducity.config.remoteconfig

/**
 * Example remote configs for demonstration purposes.
 * 
 * To add a new config:
 * 1. Create a class extending RemoteConfig
 * 2. Define the key and default value
 * 3. Register it in the DI module (ConfigModule)
 * 4. Create the parameter in Firebase Console
 * 
 * Example:
 * ```
 * class MyFeatureConfig(runner: RemoteConfigRunner) : RemoteConfig(
 *   remoteConfigRunner = runner,
 *   key = "my_feature_enabled",
 *   defaultValue = false
 * )
 * ```
 */

/**
 * Example boolean config for a feature flag.
 */
class ExampleFeatureConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "example_feature_enabled",
  defaultValue = false
)

/**
 * Example string config for a message.
 */
class ExampleMessageConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "example_message",
  defaultValue = "Hello from Remote Config!"
)

/**
 * Example long config for a number.
 */
class ExampleNumberConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "example_number",
  defaultValue = 42L
)

/**
 * Default values helper for setting up Firebase Remote Config defaults.
 */
object RemoteConfigDefaults {
  val defaults: Map<String, Any> = mapOf(
    "example_feature_enabled" to false,
    "example_message" to "Hello from Remote Config!",
    "example_number" to 42L,
  )
}
