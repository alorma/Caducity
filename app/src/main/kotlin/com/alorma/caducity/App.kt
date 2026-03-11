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
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.alorma.caducity.ui.screen.ai.AiAssistantRoute
import com.alorma.caducity.ui.screen.ai.AiAssistantScreen
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
  deepLinkAction: DeepLinkAction? = null,
  onboardingFlag: OnboardingFlag = koinInject(),
) {
  AppTheme(
    themePreferences = koinInject(),
  ) {
    val appBackStack =
      retain {
        val stack =
          mutableStateListOf<NavKey>(
            if (onboardingFlag.isEnabled()) OnboardingRoute else DashboardRoute,
          )
        when (deepLinkAction) {
          is DeepLinkAction.OpenProduct ->
            stack.add(
              CategoryDetailRoute(deepLinkAction.categoryId, deepLinkAction.productId),
            )
          is DeepLinkAction.OpenCategory ->
            stack.add(
              CategoryDetailRoute(deepLinkAction.categoryId, null),
            )
          null -> Unit
        }
        stack
      }

    NavDisplay(
      modifier = Modifier.fillMaxSize(),
      backStack = appBackStack,
      onBack = {
        if (appBackStack.size > 1) appBackStack.removeLast()
      },
      entryDecorators =
        listOf(
          rememberSaveableStateHolderNavEntryDecorator(),
          rememberViewModelStoreNavEntryDecorator(),
        ),
      entryProvider =
        entryProvider {
          entry<OnboardingRoute> {
            OnboardingScreen(
              onComplete = {
                appBackStack.clear()
                appBackStack.add(DashboardRoute)
              },
            )
          }
          entry<DashboardRoute> {
            DashboardScreen(
              onNavigateToCreateProduct = {
                appBackStack.add(CreateCategoryRoute)
              },
              onNavigateToCategory = { categoryId ->
                appBackStack.add(
                  CategoryDetailRoute(categoryId),
                )
              },
              onNavigateToStatus = { status ->
                appBackStack.add(FilteredItemsRoute.ByStatus(status))
              },
              onNavigateToSettings = { appBackStack.add(Settings) },
              onNavigateToAiAssistant = { appBackStack.add(AiAssistantRoute) },
            )
          }
          entry<AiAssistantRoute> {
            AiAssistantScreen(
              onNavigateBack = { appBackStack.removeLast() },
            )
          }
          entry<Settings> {
            SettingsContainer()
          }
          entry<CreateCategoryRoute> {
            CreateCategoryScreen(
              onNavigateBack = { appBackStack.removeLast() },
              onNavigateToCategory = { categoryId ->
                appBackStack.removeLast()
                appBackStack.add(CategoryDetailRoute(categoryId))
              },
            )
          }
          entry<CategoryDetailRoute> {
            CategoryDetailContainer(
              categoryId = it.categoryId,
              initialProductId = it.initialProductId,
              onBack = { appBackStack.removeLast() },
            )
          }
          entry<FilteredItemsRoute.ByStatus> {
            FilteredItemsByStatusScreen(
              status = it.status,
              onNavigateToCategory = { categoryId ->
                appBackStack.add(CategoryDetailRoute(categoryId))
              },
            )
          }
        },
    )
  }
}
