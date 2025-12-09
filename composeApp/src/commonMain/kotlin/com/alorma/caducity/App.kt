package com.alorma.caducity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.alorma.caducity.ui.screen.product.create.CreateProductDialogContent
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
      val bottomSheetStrategy = remember { BottomSheetSceneStrategy<TopLevelRoute>() }


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
            sceneStrategy = bottomSheetStrategy,
            entryProvider = entryProvider {
              entry<TopLevelRoute.Dashboard> { DashboardScreen() }
              entry<TopLevelRoute.Settings> { SettingsScreen() }
              entry<TopLevelRoute.CreateProduct>(
                metadata = BottomSheetSceneStrategy.bottomSheet(),
              ) {
                CreateProductDialogContent()
              }
            },
          )
        }
      }


      val topLevelRoutes = listOf(
        TopLevelRoute.Dashboard,
        TopLevelRoute.Settings,
      )
      if (isCompact) {
        CompactContent(
          topLevelBackStack = topLevelBackStack,
          topLevelRoutes = topLevelRoutes,
          content = content,
        )
      } else {
        ExpandedContent(
          topLevelBackStack = topLevelBackStack,
          topLevelRoutes = topLevelRoutes,
          content = content,
        )
      }
    }
  }
}

@Composable
private fun CompactContent(
  topLevelBackStack: TopLevelBackStack<TopLevelRoute>,
  topLevelRoutes: List<TopLevelRoute>,
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = Modifier.fillMaxSize(),
  ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize()) {
      content(paddingValues)

      VerticalFloatingToolbar(
        modifier = Modifier.align(Alignment.BottomEnd).safeDrawingPadding().padding(bottom = 16.dp),
        expanded = true,
        floatingActionButton = {
          FloatingToolbarDefaults.VibrantFloatingActionButton(
            onClick = { topLevelBackStack.add(TopLevelRoute.CreateProduct) }
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
          topLevelRoutes.forEach { route ->
            if (topLevelBackStack.topLevelKey == route) {
              FilledIconButton(
                onClick = { topLevelBackStack.addTopLevel(route) },
              ) {
                route.Icon()
              }
            } else {
              IconButton(
                onClick = { topLevelBackStack.addTopLevel(route) },
              ) {
                route.Icon()
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
  topLevelRoutes: List<TopLevelRoute>,
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
            onClick = { topLevelBackStack.add(TopLevelRoute.CreateProduct) }
          ) {
            Icon(imageVector = AppIcons.Add, contentDescription = null)
          }
        },
      ) {

        topLevelRoutes.forEach { route ->
          NavigationRailItem(
            selected = topLevelBackStack.topLevelKey == route,
            icon = { route.Icon() },
            label = { route.Label() },
            onClick = { topLevelBackStack.addTopLevel(route) },
          )
        }
      }
      content(paddingValues)
    }
  }
}
