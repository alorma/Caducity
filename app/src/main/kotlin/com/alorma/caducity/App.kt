package com.alorma.caducity

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy
import com.alorma.caducity.config.preferences.DisclaimerPreferences
import com.alorma.caducity.config.version.AppVersionProvider
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.alorma.caducity.feature.deeplink.toRoute
import com.alorma.caducity.feature.tracking.DismissTransformIntroAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToAddAction
import com.alorma.caducity.feature.tracking.NavigateToTabAction
import com.alorma.caducity.feature.tracking.NavigateToTransformAction
import com.alorma.caducity.ui.components.actions.ItemActionsSheet
import com.alorma.caducity.ui.components.feedback.snackbar.ScreenResults
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
import com.alorma.caducity.ui.navigation.SettingsDebug
import com.alorma.caducity.ui.navigation.SettingsDebugRemoteConfigs
import com.alorma.caducity.ui.navigation.SettingsPrivacy
import com.alorma.caducity.ui.navigation.StatusList
import com.alorma.caducity.ui.navigation.TopLevelBackStack
import com.alorma.caducity.ui.navigation.TopLevelRoute
import com.alorma.caducity.ui.navigation.Transform
import com.alorma.caducity.ui.navigation.TransformIntro
import com.alorma.caducity.ui.navigation.modalTransitions
import com.alorma.caducity.ui.navigation.nativeTransition
import com.alorma.caducity.ui.navigation.predictiveBack
import com.alorma.caducity.ui.navigation.tabTransitions
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
import com.alorma.caducity.ui.screen.settings.privacy.PrivacySettingsScreen
import com.alorma.caducity.ui.screen.status.StatusListScreen
import com.alorma.caducity.ui.screen.transform.TransformArgs
import com.alorma.caducity.ui.screen.transform.TransformScreen
import com.alorma.caducity.ui.screen.transform.intro.TransformIntroFlag
import com.alorma.caducity.ui.screen.transform.intro.TransformIntroSheet
import com.alorma.caducity.ui.screen.transform.intro.shouldShowTransformIntro
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
  transformIntroFlag: TransformIntroFlag = koinInject(),
  disclaimerPreferences: DisclaimerPreferences = koinInject(),
  eventTracker: EventTracker = koinInject(),
  screenResults: ScreenResults = koinInject(),
  searchRequests: PantrySearchRequests = koinInject(),
  versionProvider: AppVersionProvider = koinInject(),
  debugModeProvider: DebugModeProvider = koinInject(),
) {
  AppTheme(
    themePreferences = koinInject(),
  ) {
    // Saveable: survives process death. The root only matters on the first composition.
    val backStack =
      rememberNavBackStack(
        if (onboardingFlag.isEnabled() || disclaimerPreferences.needsAcceptance()) Onboarding else Home,
      )
    val navigation = remember(backStack) { TopLevelBackStack(backStack) }
    // The «Novetat Transforma» modal waits for a normal opening: not after a deep link, widget or notification.
    var openedFromOutside by retain { mutableStateOf(deepLinkAction != null) }
    LaunchedEffect(deepLinkAction) {
      deepLinkAction?.let {
        openedFromOutside = true
        navigation.openFromOutside(it.toRoute())
        onDeepLinkHandled()
      }
    }
    val activity = LocalActivity.current
    val uriHandler = LocalUriHandler.current
    val visibleScreen =
      navigation.backStack.last { it !is ItemActions && it !is QuickCategory && it !is TransformIntro }

    var introPending by retain { mutableStateOf(transformIntroFlag.isEnabled()) }
    val onboardingShowing = navigation.backStack.any { it is Onboarding }
    // The onboarding disables the flag when it finishes: read it again so a new install does not see the modal.
    LaunchedEffect(onboardingShowing) {
      if (!onboardingShowing) introPending = transformIntroFlag.isEnabled()
    }
    val showTransformIntro =
      shouldShowTransformIntro(
        introPending = introPending,
        onboardingShowing = onboardingShowing,
        openedFromOutside = openedFromOutside,
        // Only Home with nothing on top: no other screen, dialog, sheet or selection is open.
        overlayOpen = navigation.current !is Home,
      )
    LaunchedEffect(showTransformIntro) {
      if (showTransformIntro) {
        // The flag is the truth: the onboarding may have disabled it after `introPending` was read.
        val pending = transformIntroFlag.isEnabled()
        // Seen as soon as it opens, however it closes (button, outside tap or back).
        introPending = false
        transformIntroFlag.disable()
        if (pending) navigation.navigate(TransformIntro)
      }
    }

    AppShell(
      currentTab = navigation.currentTab,
      screenResults = screenResults,
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
          // Back or a tap outside the «Novetat Transforma» modal is the same as «Ara no».
          if (navigation.current is TransformIntro) eventTracker.trackAction(DismissTransformIntroAction())
          if (!navigation.pop()) activity?.finish()
        },
        transitionSpec = { nativeTransition(forward = true, vertical = false) },
        popTransitionSpec = { nativeTransition(forward = false, vertical = false) },
        predictivePopTransitionSpec = { edge -> predictiveBack(edge, vertical = false) },
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
                onOpenTransform = { navigation.navigate(Transform()) },
              )
            }
            entry<Pantry>(metadata = tabTransitions) {
              PantryScreen(
                onOpenCategory = { navigation.navigate(Category(it)) },
                onOpenNewCategory = { navigation.navigate(CategoryEditor()) },
                onOpenItemActions = { navigation.navigate(ItemActions(it)) },
              )
            }
            entry<Settings> {
              SettingsScreen(
                onOpenBackup = { navigation.navigate(SettingsBackup) },
                onOpenPrivacy = { navigation.navigate(SettingsPrivacy) },
                onOpenAbout = { navigation.navigate(SettingsAbout) },
                onOpenDebug = { navigation.navigate(SettingsDebug) },
              )
            }
            entry<SettingsBackup> { BackupScreen() }
            entry<SettingsPrivacy> { PrivacySettingsScreen(onClose = { navigation.pop() }) }
            entry<SettingsAbout> {
              AboutScreen(
                appVersion = versionProvider.getVersionName(),
                onOpenWebsite = { url -> uriHandler.openUri(url) },
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
                onOpenItemActions = { itemIds -> navigation.navigate(ItemActions(itemIds)) },
              )
            }
            entry<StatusList> {
              StatusListScreen(
                status = it.status,
                onBack = { navigation.pop() },
                onOpenItemActions = { itemIds -> navigation.navigate(ItemActions(itemIds)) },
                onOpenAdd = { navigation.navigate(Add(source = "status_empty")) },
              )
            }
            entry<Calendar> {
              CalendarScreen(
                args = CalendarArgs(categoryId = it.categoryId, date = it.date),
                onBack = { navigation.pop() },
                onOpenItemActions = { itemIds -> navigation.navigate(ItemActions(itemIds)) },
              )
            }
            entry<Add>(metadata = modalTransitions) {
              AddScreen(
                args = AddArgs(categoryId = it.categoryId, itemId = it.itemId),
                onClose = { navigation.pop() },
                onOpenQuickCategory = { navigation.navigate(QuickCategory) },
              )
            }
            entry<Transform>(metadata = modalTransitions) {
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
            entry<CategoryEditor>(metadata = modalTransitions) {
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
                itemIds = it.itemIds,
                onClose = { navigation.pop() },
                onEdit = { itemId ->
                  navigation.pop()
                  navigation.navigate(Add(itemId = itemId, source = "item_actions"))
                },
                onTransform = { itemIds ->
                  navigation.pop()
                  navigation.navigate(Transform(itemIds))
                },
              )
            }
            entry<QuickCategory>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
              QuickCategorySheet(onClose = { navigation.pop() })
            }
            entry<TransformIntro>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
              TransformIntroSheet(
                onTry = {
                  eventTracker.trackAction(NavigateToTransformAction(origin = "home", source = "intro_modal"))
                  navigation.pop()
                  navigation.navigate(Transform())
                },
                onNotNow = {
                  eventTracker.trackAction(DismissTransformIntroAction())
                  navigation.pop()
                },
              )
            }
          },
      )
    }
  }
}
