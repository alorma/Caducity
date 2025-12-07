package com.alorma.caducity

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.dashboard.DashboardScreen
import com.alorma.caducity.settings.SettingsScreen
import com.alorma.caducity.ui.icons.AppIcons

@Composable
fun App() {
  MaterialTheme {
    val topLevelBackStack = remember { mutableListOf<TopLevelRoute>(TopLevelRoute.Dashboard) }
    var selectedRoute by remember { mutableStateOf<TopLevelRoute>(TopLevelRoute.Dashboard) }

    Scaffold(
      modifier = Modifier.fillMaxSize().safeDrawingPadding(),
      bottomBar = {
        NavigationBar {
          NavigationBarItem(
            selected = selectedRoute == TopLevelRoute.Dashboard,
            icon = { Icon(AppIcons.Dashboard, contentDescription = "Dashboard") },
            label = { Text(text = "Dashboard") },
            onClick = {
              if (selectedRoute != TopLevelRoute.Dashboard) {
                topLevelBackStack.clear()
                topLevelBackStack.add(TopLevelRoute.Dashboard)
                selectedRoute = TopLevelRoute.Dashboard
              }
            },
          )
          NavigationBarItem(
            selected = selectedRoute == TopLevelRoute.Settings,
            icon = { Icon(AppIcons.Settings, contentDescription = "Settings") },
            label = { Text(text = "Settings") },
            onClick = {
              if (selectedRoute != TopLevelRoute.Settings) {
                topLevelBackStack.clear()
                topLevelBackStack.add(TopLevelRoute.Settings)
                selectedRoute = TopLevelRoute.Settings
              }
            },
          )
        }
      },
    ) {
      NavDisplay(
        backStack = topLevelBackStack,
        onBack = { topLevelBackStack.removeLast() },
        entryProvider = entryProvider {
          entry<TopLevelRoute.Dashboard> { DashboardScreen() }
          entry<TopLevelRoute.Settings> { SettingsScreen() }
        },
      )
    }
  }
}
