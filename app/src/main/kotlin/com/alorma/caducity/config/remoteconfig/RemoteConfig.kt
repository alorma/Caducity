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
  val defaultValue: Any,
) {
  /**
   * Checks if this config is enabled (for boolean configs).
   * @return True if enabled, false otherwise.
   */
  open fun isEnabled(): Boolean = remoteConfigRunner.isEnabled(this)
  
  /**
   * Gets the string value of this config (for string configs).
   * @return The string value.
   */
  open fun asString(): String = remoteConfigRunner.getString(this)
  
  /**
   * Gets the long value of this config (for long configs).
   * @return The long value.
   */
  open fun asLong(): Long = remoteConfigRunner.getLong(this)
  
  /**
   * Gets the double value of this config (for double configs).
   * @return The double value.
   */
  open fun asDouble(): Double = remoteConfigRunner.getDouble(this)
}
