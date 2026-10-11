package com.alorma.caducity

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.alorma.caducity.feature.deeplink.toRoute
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToAddAction
import com.alorma.caducity.feature.tracking.NavigateToTabAction
import com.alorma.caducity.ui.navigation.Add
import com.alorma.caducity.ui.navigation.AppShell
import com.alorma.caducity.ui.navigation.Calendar
import com.alorma.caducity.ui.navigation.CalendarPlaceholderScreen
import com.alorma.caducity.ui.navigation.Category
import com.alorma.caducity.ui.navigation.CategoryEditor
import com.alorma.caducity.ui.navigation.Expired
import com.alorma.caducity.ui.navigation.FilteredItems
import com.alorma.caducity.ui.navigation.Home
import com.alorma.caducity.ui.navigation.ItemActions
import com.alorma.caducity.ui.navigation.ItemActionsPlaceholder
import com.alorma.caducity.ui.navigation.Onboarding
import com.alorma.caducity.ui.navigation.Pantry
import com.alorma.caducity.ui.navigation.PantryListPlaceholder
import com.alorma.caducity.ui.navigation.QuickCategory
import com.alorma.caducity.ui.navigation.Settings
import com.alorma.caducity.ui.navigation.TopLevelBackStack
import com.alorma.caducity.ui.navigation.TopLevelRoute
import com.alorma.caducity.ui.screen.add.AddArgs
import com.alorma.caducity.ui.screen.add.AddScreen
import com.alorma.caducity.ui.screen.category.create.CreateCategoryScreen
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailContainer
import com.alorma.caducity.ui.screen.category.quick.QuickCategorySheet
import com.alorma.caducity.ui.screen.dashboard.DashboardScreen
import com.alorma.caducity.ui.screen.filtered.FilteredItemsByStatusScreen
import com.alorma.caducity.ui.screen.onboarding.OnboardingFlag
import com.alorma.caducity.ui.screen.onboarding.OnboardingScreen
import com.alorma.caducity.ui.screen.settings.SettingsContainer
import com.alorma.caducity.ui.theme.AppTheme
import org.koin.compose.koinInject

@Suppress("DeferStateReads")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
  deepLinkAction: DeepLinkAction? = null,
  onDeepLinkHandled: () -> Unit = {},
  onboardingFlag: OnboardingFlag = koinInject(),
  eventTracker: EventTracker = koinInject(),
) {
  AppTheme(
    themePreferences = koinInject(),
  ) {
    val navigation =
      retain {
        TopLevelBackStack(initialRoot = if (onboardingFlag.isEnabled()) Onboarding else Home)
      }
    LaunchedEffect(deepLinkAction) {
      deepLinkAction?.let {
        navigation.openFromOutside(it.toRoute())
        onDeepLinkHandled()
      }
    }
    val activity = LocalActivity.current
    val visibleScreen = navigation.backStack.last { it !is ItemActions && it !is QuickCategory }

    AppShell(
      currentTab = navigation.currentTab,
      showNavigation = visibleScreen is TopLevelRoute,
      onTabSelected = { tab ->
        if (tab != navigation.currentTab) {
          eventTracker.trackAction(NavigateToTabAction(if (tab == Home) "soon" else "pantry"))
          navigation.switchTab(tab)
        }
      },
      onSettingsClick = {
        eventTracker.trackAction(NavigateToTabAction("settings"))
        navigation.navigate(Settings)
      },
      onAddClick = {
        eventTracker.trackAction(NavigateToAddAction())
        navigation.navigate(Add(source = "fab"))
      },
    ) {
      NavDisplay(
        modifier = Modifier,
        backStack = navigation.backStack,
        onBack = {
          if (!navigation.pop()) activity?.finish()
        },
        entryDecorators =
          listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
          ),
        sceneStrategies = listOf(BottomSheetSceneStrategy()),
        entryProvider =
          entryProvider {
            entry<Onboarding> {
              OnboardingScreen(onComplete = { navigation.resetTo() })
            }
            // TEMP(fase 5b): old dashboard until Home exists.
            entry<Home> {
              DashboardScreen(
                onNavigateToCreateProduct = { navigation.navigate(CategoryEditor()) },
                onNavigateToCategory = { categoryId -> navigation.navigate(Category(categoryId)) },
                onNavigateToStatus = { status -> navigation.navigate(FilteredItems(status)) },
                onNavigateToSettings = { navigation.navigate(Settings) },
              )
            }
            entry<Pantry> {
              PantryListPlaceholder(
                onCategoryClick = { navigation.navigate(Category(it)) },
                onSettingsClick = { navigation.navigate(Settings) },
              )
            }
            // TEMP(fase 8): old settings with its nested NavDisplay.
            entry<Settings> {
              SettingsContainer()
            }
            entry<Category> {
              CategoryDetailContainer(
                categoryId = it.id,
                initialProductId = it.initialProductId,
                onBack = { navigation.pop() },
                onAddItem = { navigation.navigate(Add(categoryId = it, source = "category")) },
              )
            }
            // TEMP(fase 7): old items-by-status screen stands in for Expired.
            entry<Expired> {
              FilteredItemsByStatusScreen(
                status = ItemStatus.Expired,
                onNavigateToCategory = { categoryId -> navigation.navigate(Category(categoryId)) },
              )
            }
            entry<FilteredItems> {
              FilteredItemsByStatusScreen(
                status = it.status,
                onNavigateToCategory = { categoryId -> navigation.navigate(Category(categoryId)) },
              )
            }
            entry<Calendar> { CalendarPlaceholderScreen() }
            entry<Add> {
              AddScreen(
                args = AddArgs(categoryId = it.categoryId, itemId = it.itemId),
                onClose = { navigation.pop() },
                onOpenQuickCategory = { navigation.navigate(QuickCategory) },
              )
            }
            // TEMP(fase 8): the old create screen stands in for the category editor.
            entry<CategoryEditor> {
              CreateCategoryScreen(
                onNavigateBack = { navigation.pop() },
                onNavigateToCategory = { categoryId ->
                  navigation.pop()
                  navigation.navigate(Category(categoryId))
                },
              )
            }
            entry<ItemActions>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
              ItemActionsPlaceholder(it.itemId)
            }
            entry<QuickCategory>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
              QuickCategorySheet(onClose = { navigation.pop() })
            }
          },
      )
    }
  }
}
