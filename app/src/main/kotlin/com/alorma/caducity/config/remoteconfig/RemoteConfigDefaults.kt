package com.alorma.caducity.config.remoteconfig

/**
 * Remote config definitions.
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
