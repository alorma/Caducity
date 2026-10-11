package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarScrollBehavior
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
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.HourglassBottom
import com.alorma.caducity.base.ui.icons.symbols.Kitchen
import com.alorma.caducity.base.ui.icons.symbols.Settings
import com.alorma.caducity.base.ui.icons.symbols.Symbols

private data class NavDestination(
  val route: TopLevelRoute,
  val label: Int,
  val icon: ImageVector,
  val selectedIcon: ImageVector,
)

private val Destinations =
  listOf(
    NavDestination(Home, R.string.nav_soon, Symbols.Outlined.HourglassBottom, Symbols.Filled.HourglassBottom),
    NavDestination(Pantry, R.string.nav_pantry, Symbols.Outlined.Kitchen, Symbols.Filled.Kitchen),
    NavDestination(Settings, R.string.nav_settings, Symbols.Outlined.Settings, Symbols.Filled.Settings),
  )

/** Floating bar with the three top-level destinations and the separated add FAB (64 dp, radius 20). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingNavBar(
  currentTab: TopLevelRoute,
  onTabSelected: (TopLevelRoute) -> Unit,
  onAddClick: () -> Unit,
  scrollBehavior: FloatingToolbarScrollBehavior,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    HorizontalFloatingToolbar(
      expanded = true,
      scrollBehavior = scrollBehavior,
      colors =
        FloatingToolbarDefaults.standardFloatingToolbarColors(
          toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer,
          toolbarContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
      Destinations.forEach { destination ->
        NavItem(
          destination = destination,
          selected = destination.route == currentTab,
          onClick = { onTabSelected(destination.route) },
        )
      }
    }
    FloatingActionButton(
      onClick = onAddClick,
      modifier = Modifier.size(64.dp),
      shape = RoundedCornerShape(20.dp),
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
    ) {
      Icon(
        imageVector = Symbols.Outlined.Add,
        contentDescription = stringResource(R.string.action_add_product),
        modifier = Modifier.size(28.dp),
      )
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
