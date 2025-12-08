package com.alorma.caducity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.dashboard.DashboardScreen
import com.alorma.caducity.settings.SettingsScreen
import com.alorma.caducity.ui.adaptive.isWidthCompact
import com.alorma.caducity.ui.icons.AppIcons
import com.alorma.caducity.ui.theme.AppTheme

@Composable
fun App() {
  AppTheme {
    val topLevelBackStack = remember { TopLevelBackStack<TopLevelRoute>(TopLevelRoute.Dashboard) }

    val isCompact = isWidthCompact()

    val content: @Composable (PaddingValues) -> Unit = @Composable { paddingValues ->
      Box(
        modifier = Modifier,
      ) {
        NavDisplay(
          modifier = Modifier.fillMaxSize(),
          backStack = topLevelBackStack.backStack,
          onBack = { topLevelBackStack.removeLast() },
          entryProvider = entryProvider {
            entry<TopLevelRoute.Dashboard> { DashboardScreen() }
            entry<TopLevelRoute.Settings> { SettingsScreen() }
          },
        )
      }
    }

    if (isCompact) {
      CompactContent(
        topLevelBackStack = topLevelBackStack,
        content = content,
      )
    } else {
      ExpandedContent(
        topLevelBackStack = topLevelBackStack,
        content = content,
      )
    }
  }
}

@Composable
private fun CompactContent(
  topLevelBackStack: TopLevelBackStack<TopLevelRoute>,
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          selected = topLevelBackStack.topLevelKey == TopLevelRoute.Dashboard,
          icon = {
            Icon(
              imageVector = AppIcons.Dashboard,
              contentDescription = "Dashboard",
            )
          },
          label = { Text(text = "Dashboard") },
          onClick = {
            topLevelBackStack.addTopLevel(TopLevelRoute.Dashboard)
          },
        )
        NavigationBarItem(
          selected = topLevelBackStack.topLevelKey == TopLevelRoute.Settings,
          icon = {
            Icon(
              imageVector = AppIcons.Settings,
              contentDescription = "Settings",
            )
          },
          label = { Text(text = "Settings") },
          onClick = {
            topLevelBackStack.addTopLevel(TopLevelRoute.Settings)
          },
        )
      }
    },
  ) { paddingValues ->
    content(paddingValues)
  }
}

@Composable
private fun ExpandedContent(
  topLevelBackStack: TopLevelBackStack<TopLevelRoute>,
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
  ) { paddingValues ->
    Row(
      modifier = Modifier.padding(paddingValues),
    ) {
      NavigationRail(
        modifier = Modifier.padding(paddingValues),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        header = { Spacer(modifier = Modifier.height(16.dp)) },
      ) {
        NavigationRailItem(
          selected = topLevelBackStack.topLevelKey == TopLevelRoute.Dashboard,
          icon = {
            Icon(
              imageVector = AppIcons.Dashboard,
              contentDescription = "Dashboard",
            )
          },
          label = { Text(text = "Dashboard") },
          onClick = {
            topLevelBackStack.addTopLevel(TopLevelRoute.Dashboard)
          },
        )
        NavigationRailItem(
          selected = topLevelBackStack.topLevelKey == TopLevelRoute.Settings,
          icon = {
            Icon(
              imageVector = AppIcons.Settings,
              contentDescription = "Settings",
            )
          },
          label = { Text(text = "Settings") },
          onClick = {
            topLevelBackStack.addTopLevel(TopLevelRoute.Settings)
          },
        )
      }
      content(paddingValues)
    }
  }
}
