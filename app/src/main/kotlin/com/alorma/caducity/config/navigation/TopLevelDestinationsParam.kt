package com.alorma.caducity.config.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface TopLevelDestinationsParam : Parcelable {

  @Parcelize
  data object Dashboard : TopLevelDestinationsParam

  @Parcelize
  data object Products : TopLevelDestinationsParam

  @Parcelize
  data object Settings : TopLevelDestinationsParam
}