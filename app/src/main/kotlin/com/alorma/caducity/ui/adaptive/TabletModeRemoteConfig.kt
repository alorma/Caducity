package com.alorma.caducity.ui.adaptive

import com.alorma.caducity.BuildConfig
import com.alorma.caducity.config.remoteconfig.RemoteConfig
import com.alorma.caducity.config.remoteconfig.RemoteConfigRunner

class TabletModeRemoteConfig(
  remoteConfigRunner: RemoteConfigRunner
) : RemoteConfig(
  remoteConfigRunner = remoteConfigRunner,
  key = "rc_tablet_mode_enabled",
  defaultValue = BuildConfig.DEBUG,
)
