package com.alorma.caducity

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.ui.icons.Add
import com.alorma.caducity.ui.icons.AppIcons
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute : NavKey {

  @Serializable
  data object Dashboard : TopLevelRoute

  @Serializable
  data object Settings : TopLevelRoute

  @Serializable
  data object CreateProduct : TopLevelRoute
}

@Composable
fun TopLevelRoute.iconImageVector() = when (this) {
  TopLevelRoute.CreateProduct -> AppIcons.Add
  TopLevelRoute.Dashboard -> AppIcons.Dashboard
  TopLevelRoute.Settings -> AppIcons.Settings
}

@Composable
fun TopLevelRoute.iconContentDescription() = when (this) {
  TopLevelRoute.CreateProduct -> "Add"
  TopLevelRoute.Dashboard -> "Dashboard"
  TopLevelRoute.Settings -> "Settings"
}

@Composable
fun TopLevelRoute.Icon() {
  Icon(
    imageVector = iconImageVector(),
    contentDescription = iconContentDescription(),
  )
}

@Composable
fun TopLevelRoute.Label() {
  Text(text = iconContentDescription())
}