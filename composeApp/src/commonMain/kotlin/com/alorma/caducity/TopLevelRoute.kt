package com.alorma.caducity

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute : NavKey{

  @Serializable
  data object Dashboard : TopLevelRoute

  @Serializable
  data object Settings : TopLevelRoute
}