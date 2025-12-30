package com.alorma.caducity

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.base.ui.icons.AppIcons
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute : NavKey {

  @Serializable
  data object Dashboard : TopLevelRoute

  @Serializable
  data object Settings : TopLevelRoute
}

@Composable
fun TopLevelRoute.selectedIconImageVector() = when (this) {
  TopLevelRoute.Dashboard -> AppIcons.Dashboard
  TopLevelRoute.Settings -> AppIcons.Settings
}

@Composable
fun TopLevelRoute.unSelectedIconImageVector() = when (this) {
  TopLevelRoute.Dashboard -> AppIcons.Dashboard
  TopLevelRoute.Settings -> AppIcons.Settings
}

@Composable
fun TopLevelRoute.textLabel() = when (this) {
  TopLevelRoute.Dashboard -> "Dashboard"
  TopLevelRoute.Settings -> "Settings"
}

@Suppress("ModifierRequired")
@Composable
fun TopLevelRoute.Icon(
  selected: Boolean,
  modifier: Modifier = Modifier,
) {
  Icon(
    modifier = modifier,
    imageVector = if (selected) {
      selectedIconImageVector()
    } else {
      unSelectedIconImageVector()
    },
    contentDescription = textLabel(),
  )
}

@Suppress("ModifierRequired")
@Composable
fun TopLevelRoute.Label(
  modifier: Modifier = Modifier,
) {
  Text(
    modifier = modifier,
    text = textLabel(),
  )
}