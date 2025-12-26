package com.alorma.caducity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.theme.AppTheme
import com.alorma.caducity.di.appModule
import com.alorma.caducity.di.platformModule
import com.alorma.caducity.ui.screen.dashboard.DashboardScreen
import com.alorma.caducity.ui.screen.product.create.CreateProductDialogContent
import com.alorma.caducity.ui.screen.productdetail.ProductDetailRoute
import com.alorma.caducity.ui.screen.productdetail.ProductDetailScreen
import com.alorma.caducity.ui.screen.settings.SettingsScreen
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
  KoinApplication(
    application = {
      modules(appModule, platformModule)
    }
  ) {
    AppTheme(
      themePreferences = koinInject(),
    ) {
      val topLevelBackStack = remember { TopLevelBackStack(TopLevelRoute.Dashboard) }
      val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

      val topLevelRoutes = listOf(
        TopLevelRoute.Dashboard,
        TopLevelRoute.Settings,
      )

      val exitAlwaysScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
      )
      Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(exitAlwaysScrollBehavior),
        contentWindowInsets = WindowInsets(),
      ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
          Box(
            modifier = Modifier.padding(paddingValues),
          ) {
            NavigationBar(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -ScreenOffset)
                .zIndex(1f),
              scrollBehaviour = exitAlwaysScrollBehavior,
              topLevelRoutes = topLevelRoutes,
              isRouteSelected = { topLevelBackStack.topLevelKey == it },
              onTopLevelUpdate = { topLevelBackStack.addTopLevel(it) },
            )

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
                entry<TopLevelRoute.Dashboard> {
                  DashboardScreen(
                    onNavigateToProductDetail = { productId ->
                      topLevelBackStack.add(ProductDetailRoute(productId))
                    }
                  )
                }
                entry<TopLevelRoute.Settings> { SettingsScreen() }
                entry<TopLevelRoute.CreateProduct>(
                  metadata = BottomSheetSceneStrategy.bottomSheet(),
                ) {
                  CreateProductDialogContent()
                }
                entry<ProductDetailRoute> {
                  ProductDetailScreen(
                    productId = it.productId,
                    onBack = { topLevelBackStack.removeLast() }
                  )
                }
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NavigationBar(
  topLevelRoutes: List<TopLevelRoute>,
  scrollBehaviour: FloatingToolbarScrollBehavior,
  isRouteSelected: (TopLevelRoute) -> Boolean,
  onTopLevelUpdate: (TopLevelRoute) -> Unit,
  modifier: Modifier = Modifier,
) {
  HorizontalFloatingToolbar(
    modifier = Modifier
      .safeDrawingPadding()
      .padding(bottom = 16.dp)
      .then(modifier),
    scrollBehavior = scrollBehaviour,
    expanded = true,
    floatingActionButton = {
      FloatingToolbarDefaults.VibrantFloatingActionButton(
        onClick = { onTopLevelUpdate(TopLevelRoute.CreateProduct) }
      ) {
        Icon(imageVector = AppIcons.Add, contentDescription = null)
      }
    },
    colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
    content = {
      topLevelRoutes.forEach { route ->
        if (isRouteSelected(route)) {
          FilledIconButton(
            onClick = { onTopLevelUpdate(route) },
          ) {
            route.Icon()
          }
        } else {
          IconButton(
            onClick = { onTopLevelUpdate(route) },
          ) {
            route.Icon()
          }
        }
      }
    },
  )
}
