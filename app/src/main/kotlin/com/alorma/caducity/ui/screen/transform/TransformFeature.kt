package com.alorma.caducity.ui.screen.transform

import com.alorma.caducity.config.remoteconfig.RemoteConfig
import com.alorma.caducity.config.remoteconfig.RemoteConfigRunner

/** Gates every entry to Transforma until the feature is ready to ship. Off unless Remote Config turns it on. */
class TransformFeature(
  runner: RemoteConfigRunner,
) : RemoteConfig(
    remoteConfigRunner = runner,
    key = "transform_enabled",
    defaultValue = false,
  )
