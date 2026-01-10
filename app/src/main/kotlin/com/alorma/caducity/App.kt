package com.alorma.caducity

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.onboarding.OnboardingFlag
import com.alorma.caducity.ui.screen.dashboard.DashboardRoute
import com.alorma.caducity.ui.screen.dashboard.DashboardScreen
import com.alorma.caducity.ui.screen.onboarding.OnboardingRoute
import com.alorma.caducity.ui.screen.onboarding.OnboardingScreen
import com.alorma.caducity.ui.screen.product.create.CreateProductRoute
import com.alorma.caducity.ui.screen.product.create.CreateProductScreen
import com.alorma.caducity.ui.screen.product.detail.ProductDetailContainer
import com.alorma.caducity.ui.screen.product.detail.ProductDetailRoute
import com.alorma.caducity.ui.screen.settings.Settings
import com.alorma.caducity.ui.screen.settings.SettingsContainer
import com.alorma.caducity.ui.theme.AppTheme
import org.koin.compose.koinInject

@Suppress("DeferStateReads")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
  modifier: Modifier = Modifier,
  onboardingFlag: OnboardingFlag = koinInject(),
) {
  AppTheme(
    themePreferences = koinInject(),
  ) {
    val initialRoute = if (onboardingFlag.isEnabled()) {
      OnboardingRoute
    } else {
      DashboardRoute
    }

    val appBackStack = retain {
      mutableStateListOf<NavKey>(initialRoute)
    }

    NavDisplay(
      modifier = Modifier.fillMaxSize(),
      backStack = appBackStack,
      entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
      entryProvider = entryProvider {
        entry<OnboardingRoute> {
          OnboardingScreen(
            onComplete = { appBackStack.add(DashboardRoute) },
          )
        }
        entry<DashboardRoute> {
          DashboardScreen(
            onNavigateToProduct = { productId ->
              appBackStack.add(
                ProductDetailRoute(productId)
              )
            },
            onNavigateToDate = { date ->

            },
            onNavigateToStatus = { status ->

            },
            onNavigateToSettings = { appBackStack.add(Settings) },
          )
        }
        entry<Settings> {
          SettingsContainer()
        }
        entry<CreateProductRoute> {
          CreateProductScreen(
            onProductCreated = { productId ->
              appBackStack.removeLast()
              appBackStack.add(ProductDetailRoute(productId)) // Navigate to detail
            }
          )
        }
        entry<ProductDetailRoute> {
          ProductDetailContainer(
            productId = it.productId,
            onBack = { appBackStack.removeLast() }
          )
        }
      },
    )
  }
}

