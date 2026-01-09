package com.alorma.caducity.config.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.filled.Dashboard
import com.alorma.caducity.base.ui.icons.filled.List
import com.alorma.caducity.base.ui.icons.filled.Settings
import com.alorma.caducity.base.ui.icons.outlined.Dashboard
import com.alorma.caducity.base.ui.icons.outlined.List
import com.alorma.caducity.base.ui.icons.outlined.Settings
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute : NavKey {

  @Serializable
  data object Dashboard : TopLevelRoute

  @Serializable
  data object Products : TopLevelRoute
}

@Composable
fun TopLevelRoute.selectedIconImageVector() = when (this) {
  TopLevelRoute.Dashboard -> AppIcons.Filled.Dashboard
  TopLevelRoute.Products -> AppIcons.Filled.List
}

@Composable
fun TopLevelRoute.unSelectedIconImageVector() = when (this) {
  TopLevelRoute.Dashboard -> AppIcons.Outlined.Dashboard
  TopLevelRoute.Products -> AppIcons.Outlined.List
}

@Composable
fun TopLevelRoute.textLabel() = when (this) {
  TopLevelRoute.Dashboard -> stringResource(R.string.dashboard_screen_title)
  TopLevelRoute.Products -> stringResource(R.string.products_screen_title)
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