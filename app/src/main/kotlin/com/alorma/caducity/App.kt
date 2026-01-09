package com.alorma.caducity

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy
import com.alorma.caducity.config.navigation.Icon
import com.alorma.caducity.config.navigation.Label
import com.alorma.caducity.config.navigation.TopLevelBackStack
import com.alorma.caducity.config.navigation.TopLevelRoute
import com.alorma.caducity.onboarding.OnboardingFlag
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.screen.dashboard.DashboardScreen
import com.alorma.caducity.ui.screen.onboarding.OnboardingRoute
import com.alorma.caducity.ui.screen.onboarding.OnboardingScreen
import com.alorma.caducity.ui.screen.product.create.CreateProductRoute
import com.alorma.caducity.ui.screen.product.create.CreateProductScreen
import com.alorma.caducity.ui.screen.product.detail.ProductDetailContainer
import com.alorma.caducity.ui.screen.product.detail.ProductDetailRoute
import com.alorma.caducity.ui.screen.products.ProductsListScreen
import com.alorma.caducity.ui.screen.settings.Settings
import com.alorma.caducity.ui.screen.settings.SettingsContainer
import com.alorma.caducity.ui.theme.AppTheme
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject

@Suppress("DeferStateReads")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
  initialDestination: TopLevelRoute?,
  modifier: Modifier = Modifier,
  onboardingFlag: OnboardingFlag = koinInject(),
) {
  AppTheme(
    themePreferences = koinInject(),
  ) {
    // Determine initial route based on explicit destination, onboarding status, or default to Dashboard
    val initialRoute = initialDestination ?: if (onboardingFlag.isEnabled()) {
      OnboardingRoute
    } else {
      TopLevelRoute.Dashboard
    }

    val topLevelBackStack = retain { TopLevelBackStack(initialRoute) }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

    val topLevelRoutes = persistentListOf(
      TopLevelRoute.Dashboard,
      TopLevelRoute.Products(),
    )

    val exitAlwaysScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
      exitDirection = FloatingToolbarExitDirection.Bottom,
    )
    AppScaffold(
      modifier = Modifier
        .fillMaxSize()
        .nestedScroll(exitAlwaysScrollBehavior)
        .then(modifier),
      contentWindowInsets = WindowInsets(),
      bottomBar = {
        val isTopLevelRoute = topLevelBackStack.backStack.last() is TopLevelRoute
        AnimatedVisibility(isTopLevelRoute) {
          BottomAppBar(
            containerColor = CaducityTheme.colorScheme.background,
          ) {
            Box(
              modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
              contentAlignment = Alignment.BottomCenter
            ) {
              NavigationBar(
                topLevelRoutes = topLevelRoutes,
                isRouteSelected = { topLevelBackStack.topLevelKey::class == it::class },
                onTopLevelUpdate = { topLevelBackStack.addTopLevel(it) },
              )
            }
          }
        }
      },
    ) { paddingValues ->
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
        entryProvider = entryProvider {
          entry<OnboardingRoute> {
            OnboardingScreen(
              onComplete = {
                // Navigate to Dashboard after onboarding
                topLevelBackStack.add(TopLevelRoute.Dashboard)
              }
            )
          }
          entry<TopLevelRoute.Dashboard> {
            DashboardScreen(
              scrollConnection = exitAlwaysScrollBehavior,
              onNavigateToProduct = { productId ->
                topLevelBackStack.add(
                  ProductDetailRoute(productId)
                )
              },
              onNavigateToDate = { date ->
                topLevelBackStack.addTopLevel(
                  TopLevelRoute.Products.byDate(
                    date
                  )
                )
              },
              onNavigateToStatus = { status ->
                topLevelBackStack.addTopLevel(
                  TopLevelRoute.Products.byStatus(
                    setOf(status)
                  )
                )
              },
              onNavigateToSettings = { topLevelBackStack.add(Settings) },
            )
          }
          entry<TopLevelRoute.Products> { route ->
            ProductsListScreen(
              filters = route.toFilter(),
              scrollConnection = exitAlwaysScrollBehavior,
              onNavigateToProductDetail = { productId ->
                topLevelBackStack.add(ProductDetailRoute(productId))
              },
              onCreateProduct = { topLevelBackStack.add(CreateProductRoute) },
            )
          }
          entry<Settings> {
            SettingsContainer(
              scrollConnection = exitAlwaysScrollBehavior
            )
          }
          entry<CreateProductRoute> {
            CreateProductScreen(
              onBack = { topLevelBackStack.removeLast() },
              onProductCreated = { productId ->
                topLevelBackStack.removeLast() // Remove create screen
                topLevelBackStack.add(ProductDetailRoute(productId)) // Navigate to detail
              }
            )
          }
          entry<ProductDetailRoute> {
            ProductDetailContainer(
              productId = it.productId,
              onBack = { topLevelBackStack.removeLast() }
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
  isRouteSelected: (TopLevelRoute) -> Boolean,
  onTopLevelUpdate: (TopLevelRoute) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

  val motionScheme = CaducityTheme.motionScheme

  HorizontalFloatingToolbar(
    modifier = modifier,
    expanded = true,
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
