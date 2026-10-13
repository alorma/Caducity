package com.alorma.caducity

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy
import com.alorma.caducity.config.preferences.DisclaimerPreferences
import com.alorma.caducity.config.version.AppVersionProvider
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.alorma.caducity.feature.deeplink.toRoute
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToAddAction
import com.alorma.caducity.feature.tracking.NavigateToTabAction
import com.alorma.caducity.ui.components.actions.ItemActionsSheet
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
import com.alorma.caducity.ui.navigation.Add
import com.alorma.caducity.ui.navigation.AppShell
import com.alorma.caducity.ui.navigation.Calendar
import com.alorma.caducity.ui.navigation.Category
import com.alorma.caducity.ui.navigation.CategoryEditor
import com.alorma.caducity.ui.navigation.Home
import com.alorma.caducity.ui.navigation.IngredientPicker
import com.alorma.caducity.ui.navigation.ItemActions
import com.alorma.caducity.ui.navigation.Onboarding
import com.alorma.caducity.ui.navigation.Pantry
import com.alorma.caducity.ui.navigation.QuickCategory
import com.alorma.caducity.ui.navigation.Settings
import com.alorma.caducity.ui.navigation.SettingsAbout
import com.alorma.caducity.ui.navigation.SettingsBackup
import com.alorma.caducity.ui.navigation.SettingsCategoryMargins
import com.alorma.caducity.ui.navigation.SettingsDebug
import com.alorma.caducity.ui.navigation.SettingsDebugRemoteConfigs
import com.alorma.caducity.ui.navigation.SettingsPrivacy
import com.alorma.caducity.ui.navigation.StatusList
import com.alorma.caducity.ui.navigation.TopLevelBackStack
import com.alorma.caducity.ui.navigation.TopLevelRoute
import com.alorma.caducity.ui.navigation.Transform
import com.alorma.caducity.ui.navigation.toStatus
import com.alorma.caducity.ui.screen.add.AddArgs
import com.alorma.caducity.ui.screen.add.AddScreen
import com.alorma.caducity.ui.screen.calendar.CalendarArgs
import com.alorma.caducity.ui.screen.calendar.CalendarScreen
import com.alorma.caducity.ui.screen.category.CategoryArgs
import com.alorma.caducity.ui.screen.category.CategoryScreen
import com.alorma.caducity.ui.screen.category.editor.CategoryEditorScreen
import com.alorma.caducity.ui.screen.category.quick.QuickCategorySheet
import com.alorma.caducity.ui.screen.home.HomeScreen
import com.alorma.caducity.ui.screen.onboarding.OnboardingFlag
import com.alorma.caducity.ui.screen.onboarding.OnboardingScreen
import com.alorma.caducity.ui.screen.pantry.PantryScreen
import com.alorma.caducity.ui.screen.pantry.PantrySearchRequests
import com.alorma.caducity.ui.screen.settings.SettingsScreen
import com.alorma.caducity.ui.screen.settings.about.AboutScreen
import com.alorma.caducity.ui.screen.settings.backup.BackupScreen
import com.alorma.caducity.ui.screen.settings.debug.DebugRemoteConfigsScreen
import com.alorma.caducity.ui.screen.settings.debug.DebugSettingsScreen
import com.alorma.caducity.ui.screen.settings.margins.CategoryMarginsScreen
import com.alorma.caducity.ui.screen.settings.privacy.PrivacySettingsScreen
import com.alorma.caducity.ui.screen.status.StatusListScreen
import com.alorma.caducity.ui.screen.transform.TransformArgs
import com.alorma.caducity.ui.screen.transform.TransformScreen
import com.alorma.caducity.ui.screen.transform.picker.IngredientPickerArgs
import com.alorma.caducity.ui.screen.transform.picker.IngredientPickerScreen
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
  disclaimerPreferences: DisclaimerPreferences = koinInject(),
  eventTracker: EventTracker = koinInject(),
  snackbarEvents: GlobalSnackbarEvents = koinInject(),
  searchRequests: PantrySearchRequests = koinInject(),
  versionProvider: AppVersionProvider = koinInject(),
  debugModeProvider: DebugModeProvider = koinInject(),
) {
  AppTheme(
    themePreferences = koinInject(),
  ) {
    val navigation =
      retain {
        TopLevelBackStack(
          initialRoot =
            if (onboardingFlag.isEnabled() || disclaimerPreferences.needsAcceptance()) Onboarding else Home,
        )
      }
    LaunchedEffect(deepLinkAction) {
      deepLinkAction?.let {
        navigation.openFromOutside(it.toRoute())
        onDeepLinkHandled()
      }
    }
    val activity = LocalActivity.current
    val uriHandler = LocalUriHandler.current
    val visibleScreen = navigation.backStack.last { it !is ItemActions && it !is QuickCategory }

    AppShell(
      currentTab = navigation.currentTab,
      snackbarEvents = snackbarEvents,
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
            entry<Home> {
              HomeScreen(
                onOpenCalendar = { date -> navigation.navigate(Calendar(date = date)) },
                onOpenSearch = {
                  searchRequests.request()
                  navigation.switchTab(Pantry)
                },
                onOpenStatusList = { navigation.navigate(StatusList(it)) },
                onOpenPantry = { navigation.switchTab(Pantry) },
                onOpenItemActions = { navigation.navigate(ItemActions(it)) },
                onOpenAdd = { navigation.navigate(Add(source = "home_empty")) },
              )
            }
            entry<Pantry> {
              PantryScreen(
                onOpenCategory = { navigation.navigate(Category(it)) },
                onOpenNewCategory = { navigation.navigate(CategoryEditor()) },
                onOpenItemActions = { navigation.navigate(ItemActions(it)) },
              )
            }
            entry<Settings> {
              SettingsScreen(
                onOpenBackup = { navigation.navigate(SettingsBackup) },
                onOpenCategoryMargins = { navigation.navigate(SettingsCategoryMargins) },
                onOpenPrivacy = { navigation.navigate(SettingsPrivacy) },
                onOpenAbout = { navigation.navigate(SettingsAbout) },
                onOpenDebug = { navigation.navigate(SettingsDebug) },
              )
            }
            entry<SettingsBackup> { BackupScreen() }
            entry<SettingsCategoryMargins> { CategoryMarginsScreen() }
            entry<SettingsPrivacy> { PrivacySettingsScreen(onClose = { navigation.pop() }) }
            entry<SettingsAbout> {
              AboutScreen(
                appVersion = versionProvider.getVersionName(),
                onNavigateToRepo = { uriHandler.openUri("https://github.com/alorma/caducity") },
              )
            }
            if (debugModeProvider.isDebugMode()) {
              entry<SettingsDebug> {
                DebugSettingsScreen(
                  onNavigateToRemoteConfigs = { navigation.navigate(SettingsDebugRemoteConfigs) },
                  onNavigateToTransform = { itemIds -> navigation.navigate(Transform(itemIds)) },
                )
              }
              entry<SettingsDebugRemoteConfigs> { DebugRemoteConfigsScreen() }
            }
            entry<Category> {
              CategoryScreen(
                args = CategoryArgs(it.id, it.filter?.toStatus(), it.initialProductId),
                onBack = { navigation.pop() },
                onOpenAdd = { navigation.navigate(Add(categoryId = it.id, source = "category")) },
                onOpenCalendar = { navigation.navigate(Calendar(categoryId = it.id)) },
                onOpenEditor = { navigation.navigate(CategoryEditor(it.id)) },
                onOpenItemActions = { itemId -> navigation.navigate(ItemActions(itemId)) },
              )
            }
            entry<StatusList> {
              StatusListScreen(
                status = it.status,
                onBack = { navigation.pop() },
                onOpenItemActions = { itemId -> navigation.navigate(ItemActions(itemId)) },
                onOpenAdd = { navigation.navigate(Add(source = "status_empty")) },
                onOpenTransform = { itemIds -> navigation.navigate(Transform(itemIds)) },
              )
            }
            entry<Calendar> {
              CalendarScreen(
                args = CalendarArgs(categoryId = it.categoryId, date = it.date),
                onBack = { navigation.pop() },
                onOpenItemActions = { itemId -> navigation.navigate(ItemActions(itemId)) },
              )
            }
            entry<Add> {
              AddScreen(
                args = AddArgs(categoryId = it.categoryId, itemId = it.itemId),
                onClose = { navigation.pop() },
                onOpenQuickCategory = { navigation.navigate(QuickCategory) },
              )
            }
            entry<Transform> {
              TransformScreen(
                args = TransformArgs(itemIds = it.itemIds),
                onClose = { navigation.pop() },
                onOpenIngredientPicker = { selected -> navigation.navigate(IngredientPicker(selected)) },
              )
            }
            entry<IngredientPicker> {
              IngredientPickerScreen(
                args = IngredientPickerArgs(selectedItemIds = it.selectedItemIds),
                onClose = { navigation.pop() },
              )
            }
            entry<CategoryEditor> {
              CategoryEditorScreen(
                categoryId = it.id,
                onClose = { navigation.pop() },
                onCreated = { categoryId ->
                  navigation.pop()
                  navigation.navigate(Category(categoryId))
                },
                // Deleted from the editor: the category screen under it is gone as well.
                onDeleted = {
                  navigation.pop()
                  navigation.pop()
                },
              )
            }
            entry<ItemActions>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
              ItemActionsSheet(
                itemId = it.itemId,
                onClose = { navigation.pop() },
                onEdit = { itemId ->
                  navigation.pop()
                  navigation.navigate(Add(itemId = itemId, source = "item_actions"))
                },
                onTransform = { itemId ->
                  navigation.pop()
                  navigation.navigate(Transform(listOf(itemId)))
                },
              )
            }
            entry<QuickCategory>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
              QuickCategorySheet(onClose = { navigation.pop() })
            }
          },
      )
    }
  }
}
