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
import com.alorma.caducity.ui.screen.category.create.CreateCategoryRoute
import com.alorma.caducity.ui.screen.category.create.CreateCategoryScreen
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailContainer
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailRoute
import com.alorma.caducity.ui.screen.dashboard.DashboardRoute
import com.alorma.caducity.ui.screen.dashboard.DashboardScreen
import com.alorma.caducity.ui.screen.dashboard.FilteredItemsRoute
import com.alorma.caducity.ui.screen.filtered.FilteredItemsByStatusScreen
import com.alorma.caducity.ui.screen.onboarding.OnboardingFlag
import com.alorma.caducity.ui.screen.onboarding.OnboardingRoute
import com.alorma.caducity.ui.screen.onboarding.OnboardingScreen
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
            onNavigateToCreateProduct = {
              appBackStack.add(CreateCategoryRoute)
            },
            onNavigateToCategory = { categoryId ->
              appBackStack.add(
                CategoryDetailRoute(categoryId)
              )
            },
            onNavigateToStatus = { status ->
              appBackStack.add(FilteredItemsRoute.ByStatus(status))
            },
            onNavigateToSettings = { appBackStack.add(Settings) },
          )
        }
        entry<Settings> {
          SettingsContainer()
        }
        entry<CreateCategoryRoute> {
          CreateCategoryScreen(
            onCategoryCreated = { categoryId ->
              appBackStack.removeLast()
              appBackStack.add(CategoryDetailRoute(categoryId)) // Navigate to detail
            }
          )
        }
        entry<CategoryDetailRoute> {
          CategoryDetailContainer(
            categoryId = it.categoryId,
            onBack = { appBackStack.removeLast() }
          )
        }
        entry<FilteredItemsRoute.ByStatus> {
          FilteredItemsByStatusScreen(
            status = it.status,
            onNavigateToCategory = { categoryId ->
              appBackStack.add(CategoryDetailRoute(categoryId))
            }
          )
        }
      },
    )
  }
}

