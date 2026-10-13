package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.HourglassBottom
import com.alorma.caducity.base.ui.icons.symbols.Kitchen
import com.alorma.caducity.base.ui.icons.symbols.Settings
import com.alorma.caducity.base.ui.icons.symbols.Symbols

private data class NavDestination(
  val label: Int,
  val icon: ImageVector,
  val selectedIcon: ImageVector,
)

private val Destinations =
  listOf(
    Home to NavDestination(R.string.nav_soon, Symbols.Outlined.HourglassBottom, Symbols.Filled.HourglassBottom),
    Pantry to NavDestination(R.string.nav_pantry, Symbols.Outlined.Kitchen, Symbols.Filled.Kitchen),
  )

private val SettingsDestination =
  NavDestination(R.string.nav_settings, Symbols.Outlined.Settings, Symbols.Filled.Settings)

/** Floating bar with two tabs plus the settings shortcut, and the separated add FAB (64 dp, radius 20). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingNavBar(
  currentTab: TopLevelRoute,
  onTabSelected: (TopLevelRoute) -> Unit,
  onAddClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
  onTransformClick: (() -> Unit)? = null,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    HorizontalFloatingToolbar(
      expanded = true,
      colors =
        FloatingToolbarDefaults.standardFloatingToolbarColors(
          toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer,
          toolbarContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
      Destinations.forEach { (route, destination) ->
        NavItem(
          destination = destination,
          selected = route == currentTab,
          onClick = { onTabSelected(route) },
        )
      }
      // Settings is not a tab: it opens a secondary screen, so it is never shown as selected.
      NavItem(
        destination = SettingsDestination,
        selected = false,
        onClick = onSettingsClick,
      )
    }
    if (onTransformClick != null) {
      SplitAddButton(onAddClick = onAddClick, onTransformClick = onTransformClick)
    } else {
      AddButton(onClick = onAddClick)
    }
  }
}

@Composable
private fun NavItem(
  destination: NavDestination,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val label = stringResource(destination.label)
  val colors = MaterialTheme.colorScheme
  Row(
    modifier =
      Modifier
        .height(48.dp)
        .clip(CircleShape)
        .then(
          if (selected) {
            Modifier.background(colors.primary)
          } else {
            Modifier
          },
        ).clickable(onClick = onClick)
        .semantics {
          role = Role.Tab
          this.selected = selected
          if (!selected) contentDescription = label
        }.padding(horizontal = if (selected) 16.dp else 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = if (selected) destination.selectedIcon else destination.icon,
      contentDescription = null,
      tint = if (selected) colors.onPrimary else colors.onPrimaryContainer,
      modifier = Modifier.size(24.dp),
    )
    if (selected) {
      Spacer(Modifier.width(8.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = colors.onPrimary,
      )
    }
  }
}
