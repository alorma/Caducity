package com.alorma.caducity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.ui.screen.dashboard.DashboardScreen
import com.alorma.caducity.ui.screen.product.create.CreateProductRoute
import com.alorma.caducity.ui.screen.product.create.CreateProductScreen
import com.alorma.caducity.ui.screen.product.detail.ProductDetailRoute
import com.alorma.caducity.ui.screen.product.detail.ProductDetailScreen
import com.alorma.caducity.ui.screen.products.ProductsListRoute
import com.alorma.caducity.ui.screen.products.ProductsListScreen
import com.alorma.caducity.ui.screen.settings.SettingsContainer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject

@Suppress("DeferStateReads")
@Composable
fun App(
  modifier: Modifier = Modifier,
  showExpiringOnly: Boolean = false,
) {
  AppTheme(
    themePreferences = koinInject(),
  ) {
    val topLevelBackStack = remember { TopLevelBackStack(TopLevelRoute.Dashboard) }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

    val topLevelRoutes = persistentListOf(
      TopLevelRoute.Dashboard,
      TopLevelRoute.Settings,
    )

    val exitAlwaysScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
      exitDirection = FloatingToolbarExitDirection.Bottom,
    )
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .nestedScroll(exitAlwaysScrollBehavior)
        .then(modifier),
      bottomBar = {
        val isTopLevelRoute = topLevelBackStack.backStack.last() is TopLevelRoute

        val motionScheme = CaducityTheme.motionScheme

        AnimatedVisibility(
          isTopLevelRoute,
          enter = slideInVertically(motionScheme.slowSpatialSpec()) { it },
          exit = slideOutVertically(motionScheme.slowSpatialSpec()) { it }
        ) {
          val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()

          val layoutDirection = LocalLayoutDirection.current
          val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()

          Box(
            Modifier
              .fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd,
          ) {
            NavigationBar(
              modifier = Modifier
                .padding(
                  top = ScreenOffset,
                  bottom = systemBarsInsets.calculateBottomPadding() + ScreenOffset,
                  start = systemBarsInsets.calculateBottomPadding(),
                  end = systemBarsInsets.calculateBottomPadding(),
                )
                .zIndex(1f),
              scrollBehaviour = exitAlwaysScrollBehavior,
              topLevelRoutes = topLevelRoutes,
              isRouteSelected = { topLevelBackStack.topLevelKey == it },
              onTopLevelUpdate = { topLevelBackStack.addTopLevel(it) },
              onCreateProduct = { topLevelBackStack.add(CreateProductRoute) },
            )
          }
        }
      },
    ) { paddingValues ->
      val motionScheme = CaducityTheme.motionScheme

      NavDisplay(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        backStack = topLevelBackStack.backStack,
        onBack = { topLevelBackStack.removeLast() },
        entryDecorators = listOf(
          rememberSaveableStateHolderNavEntryDecorator(),
          rememberViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategy = bottomSheetStrategy,
        transitionSpec = {
          fadeIn(motionScheme.defaultEffectsSpec())
            .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
        },
        popTransitionSpec = {
          fadeIn(motionScheme.defaultEffectsSpec())
            .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
        },
        predictivePopTransitionSpec = {
          fadeIn(motionScheme.defaultEffectsSpec())
            .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
        },
        entryProvider = entryProvider {
          entry<TopLevelRoute.Dashboard> {
            DashboardScreen(
              showExpiringOnly = showExpiringOnly,
              scrollConnection = exitAlwaysScrollBehavior,
              onNavigateToDate = { date ->
                topLevelBackStack.add(ProductsListRoute.byDate(date))
              },
              onNavigateToStatus = { status ->
                topLevelBackStack.add(ProductsListRoute.byStatus(setOf(status)))
              },
            )
          }
          entry<TopLevelRoute.Settings> {
            SettingsContainer(
              scrollConnection = exitAlwaysScrollBehavior
            )
          }
          entry<CreateProductRoute> {
            CreateProductScreen(
              onBack = { topLevelBackStack.removeLast() }
            )
          }
          entry<ProductDetailRoute> {
            ProductDetailScreen(
              productId = it.productId,
              onBack = { topLevelBackStack.removeLast() }
            )
          }
          entry<ProductsListRoute>(
            metadata = BottomSheetSceneStrategy.bottomSheet(),
          ) {
            ProductsListScreen(
              filters = it.toFilter(),
              onNavigateToProductDetail = { productId ->
                topLevelBackStack.add(ProductDetailRoute(productId))
              }
            )
          }
        },
      )
    }
  }
}

@Composable
private fun NavigationBar(
  topLevelRoutes: ImmutableList<TopLevelRoute>,
  scrollBehaviour: FloatingToolbarScrollBehavior,
  isRouteSelected: (TopLevelRoute) -> Boolean,
  onTopLevelUpdate: (TopLevelRoute) -> Unit,
  onCreateProduct: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

  val motionScheme = CaducityTheme.motionScheme

  HorizontalFloatingToolbar(
    modifier = modifier,
    scrollBehavior = scrollBehaviour,
    expanded = true,
    floatingActionButton = {
      FloatingToolbarDefaults.VibrantFloatingActionButton(
        onClick = onCreateProduct
      ) {
        Icon(imageVector = AppIcons.Add, contentDescription = null)
      }
    },
    colors = colors,
    content = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        topLevelRoutes.forEach { route ->
          TooltipBox(
            positionProvider =
              TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above
              ),
            tooltip = {
              PlainTooltip { route.Label() }
            },
            state = rememberTooltipState(),
          ) {
            val routeSelected = isRouteSelected(route)

            ToggleButton(
              modifier = Modifier.height(56.dp),
              checked = routeSelected,
              onCheckedChange = { onTopLevelUpdate(route) },
              shapes = ToggleButtonDefaults.shapes(
                CircleShape,
                CircleShape,
                CircleShape
              ),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Crossfade(routeSelected) { selected ->
                  route.Icon(selected)
                }
                AnimatedVisibility(
                  visible = routeSelected,
                  enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                  exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                ) {
                  route.Label(modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
                }
              }
            }
          }
        }
      }
    },
  )
}
