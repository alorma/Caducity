package com.alorma.caducity.config.remoteconfig

/**
 * Example remote config for feature flags.
 * This is a sample config to demonstrate the remote config system.
 */
class ExampleFeatureConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "example_feature_enabled",
  defaultValue = false
)

/**
 * Sample config for a beta feature.
 */
class BetaFeatureConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "beta_feature_enabled",
  defaultValue = false
)

/**
 * Sample config for experimental UI.
 */
class ExperimentalUiConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "experimental_ui_enabled",
  defaultValue = false
)
