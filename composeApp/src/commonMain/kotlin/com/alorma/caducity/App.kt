package com.alorma.caducity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.di.appModule
import com.alorma.caducity.di.platformModule
import com.alorma.caducity.ui.adaptive.isWidthCompact
import com.alorma.caducity.ui.icons.Add
import com.alorma.caducity.ui.icons.AppIcons
import com.alorma.caducity.ui.screen.dashboard.DashboardScreen
import com.alorma.caducity.ui.screen.settings.SettingsScreen
import com.alorma.caducity.ui.theme.AppTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
  KoinApplication(
    application = {
      modules(appModule, platformModule)
    }
  ) {
    AppTheme {
      val topLevelBackStack = remember { TopLevelBackStack<TopLevelRoute>(TopLevelRoute.Dashboard) }

      val isCompact = isWidthCompact()

      val content: @Composable (PaddingValues) -> Unit = @Composable { paddingValues ->
        Box(
          modifier = Modifier.padding(paddingValues),
        ) {
          NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryDecorators = listOf(
              rememberSaveableStateHolderNavEntryDecorator(),
              rememberViewModelStoreNavEntryDecorator(),
            ),
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
}

@Composable
private fun CompactContent(
  topLevelBackStack: TopLevelBackStack<TopLevelRoute>,
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
  ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize()) {
      content(paddingValues)

      VerticalFloatingToolbar(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .offset(x = -ScreenOffset)
          .padding(bottom = 24.dp, end = 24.dp),
        expanded = true,
        floatingActionButton = {
          FloatingToolbarDefaults.VibrantFloatingActionButton(
            onClick = { }
          ) {
            Icon(imageVector = AppIcons.Add, contentDescription = null)
          }
        },
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(

        ),
        scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
          exitDirection = FloatingToolbarExitDirection.End,
        ),
        content = {
          val topLevelRoutes = listOf(
            TopLevelRoute.Dashboard,
            TopLevelRoute.Settings,
          )

          val icon: @Composable (TopLevelRoute) -> ImageVector = { route ->
            when (route) {
              TopLevelRoute.Dashboard -> AppIcons.Dashboard
              TopLevelRoute.Settings -> AppIcons.Settings
            }
          }

          val contentDescription: @Composable (TopLevelRoute) -> String = { route ->
            when (route) {
              TopLevelRoute.Dashboard -> "Dashboard"
              TopLevelRoute.Settings -> "Settings"
            }
          }

          topLevelRoutes.forEach { route ->
            if (topLevelBackStack.topLevelKey == route) {
              FilledIconButton(
                onClick = { topLevelBackStack.addTopLevel(route) },
              ) {
                Icon(
                  imageVector = icon(route),
                  contentDescription = contentDescription(route),
                )
              }
            } else {
              IconButton(
                onClick = { topLevelBackStack.addTopLevel(route) },
              ) {
                Icon(
                  imageVector = icon(route),
                  contentDescription = contentDescription(route),
                )
              }
            }
          }
        },
      )
    }
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
        header = {
          FilledIconButton(
            modifier = Modifier.padding(vertical = 24.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
            onClick = { }
          ) {
            Icon(imageVector = AppIcons.Add, contentDescription = null)
          }
        },
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
