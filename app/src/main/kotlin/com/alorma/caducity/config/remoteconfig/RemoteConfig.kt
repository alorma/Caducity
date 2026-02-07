package com.alorma.caducity.config.remoteconfig

/**
 * Abstract class representing a single remote configuration parameter.
 * 
 * Similar to FireAndForget, each config extends this class to define a specific
 * configuration parameter with its key and default value.
 * 
 * Usage example:
 * ```
 * class ExampleFeature(runner: RemoteConfigRunner) : RemoteConfig(
 *   remoteConfigRunner = runner,
 *   key = "example_feature_enabled",
 *   defaultValue = false
 * )
 * 
 * // In your code:
 * if (exampleFeature.isEnabled()) {
 *   // Feature is enabled
 * }
 * ```
 */
abstract class RemoteConfig(
  val remoteConfigRunner: RemoteConfigRunner,
  val key: String,
  val defaultValue: Boolean,
) {
  /**
   * Checks if this config is enabled.
   * @return True if enabled, false otherwise.
   */
  open fun isEnabled(): Boolean = remoteConfigRunner.isEnabled(this)
}
