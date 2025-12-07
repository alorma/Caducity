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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.dashboard.DashboardScreen
import com.alorma.caducity.settings.SettingsScreen
import com.alorma.caducity.ui.icons.AppIcons

@Composable
fun App() {
  MaterialTheme {
    val topLevelBackStack = remember { TopLevelBackStack<TopLevelRoute>(TopLevelRoute.Dashboard) }

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
              topLevelBackStack.add(TopLevelRoute.Settings)
            },
          )
        }
      },
    ) {
      NavDisplay(
        backStack = topLevelBackStack.backStack,
        onBack = { topLevelBackStack.removeLast() },
        entryProvider = entryProvider {
          entry<TopLevelRoute.Dashboard> { DashboardScreen() }
          entry<TopLevelRoute.Settings> { SettingsScreen() }
        },
      )
    }
  }
}
