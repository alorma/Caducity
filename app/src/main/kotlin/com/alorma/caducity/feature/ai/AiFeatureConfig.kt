package com.alorma.caducity.feature.ai

import com.alorma.caducity.config.remoteconfig.RemoteConfig
import com.alorma.caducity.config.remoteconfig.RemoteConfigRunner

class AiFeatureConfig(
  runner: RemoteConfigRunner,
) : RemoteConfig(
    remoteConfigRunner = runner,
    key = "ai_assistant_enabled",
    defaultValue = false,
  )
