package com.alorma.caducity.ui.screen.settings

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface SettingsRoute : NavKey {
  @Serializable
  data object Root : SettingsRoute

  @Serializable
  data object Appearance : SettingsRoute

  @Serializable
  data object Language : SettingsRoute

  @Serializable
  data object Notifications : SettingsRoute

  @Serializable
  data object Debug : SettingsRoute

  @Serializable
  data object About : SettingsRoute
}
