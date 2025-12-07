package com.alorma.caducity

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
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

@Composable
fun App() {
  MaterialTheme {
    val topLevelBackStack = remember { mutableListOf(TopLevelRoute.Dashboard) }

    Scaffold(
      modifier = Modifier.fillMaxSize().safeDrawingPadding(),
      bottomBar = {
        NavigationBar {
          NavigationBarItem(
            selected = true,
            icon = {},
            label = { Text(text = "Dashboard") },
            onClick = {},
          )
        }
      },
    ) {
      NavDisplay(
        backStack = topLevelBackStack,
        onBack = { topLevelBackStack.removeLast() },
        entryProvider = entryProvider {
          entry<TopLevelRoute.Dashboard> { DashboardScreen() }
        },
      )
    }
  }
}
