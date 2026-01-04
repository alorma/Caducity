package com.alorma.caducity.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy
import com.alorma.caducity.config.version.AppVersionProvider
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import com.alorma.caducity.feature.notification.NotificationDebugHelper
import com.alorma.caducity.ui.screen.settings.about.AboutScreen
import com.alorma.caducity.ui.screen.settings.appearance.AppearanceSettingsScreen
import com.alorma.caducity.ui.screen.settings.backup.BackupScreen
import com.alorma.caducity.ui.screen.settings.debug.DebugSettingsScreen
import com.alorma.caducity.ui.screen.settings.notifications.NotificationsSettingsScreen
import com.alorma.caducity.ui.theme.ThemePreferences
import org.koin.compose.koinInject

@Composable
fun SettingsContainer(
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
  themePreferences: ThemePreferences = koinInject(),
  notificationHelper: ExpirationNotificationHelper = koinInject(),
  debugModeProvider: DebugModeProvider = koinInject(),
  debugHelper: NotificationDebugHelper = koinInject(),
  versionProvider: AppVersionProvider = koinInject(),
) {
  val settingsBackStack = retain {
    mutableStateListOf<NavKey>(SettingsRoute.Root)
  }

  val bottomSheetStrategy = remember {
    BottomSheetSceneStrategy<NavKey>()
  }

  notificationHelper.registerContract()

  NavDisplay(
    modifier = modifier,
    backStack = settingsBackStack,
    sceneStrategy = bottomSheetStrategy,
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = entryProvider {
      entry<SettingsRoute.Root> {
        SettingsRootScreen(
          scrollConnection = scrollConnection,
          isDebug = debugModeProvider.isDebugMode(),
          onNavigateToAppearance = { settingsBackStack.add(SettingsRoute.Appearance) },
          onNavigateToNotifications = { settingsBackStack.add(SettingsRoute.Notifications) },
          onNavigateToBackup = { settingsBackStack.add(SettingsRoute.Backup) },
          onNavigateToDebug = { settingsBackStack.add(SettingsRoute.Debug) },
          onNavigateToAbout = { settingsBackStack.add(SettingsRoute.About) },
        )
      }
      entry<SettingsRoute.Appearance>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        AppearanceSettingsScreen(
          themeMode = themePreferences.themeMode.value,
          useDynamicTheme = themePreferences.useDynamicColors.value,
          onThemeModeChange = { themePreferences.setThemeModeState(it) },
          onUseDynamicTheme = { themePreferences.setDynamicColorsEnabled(it) },
          onClose = { settingsBackStack.removeLastOrNull() },
        )
      }
      entry<SettingsRoute.Notifications>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        NotificationsSettingsScreen(
          areNotificationsEnabled = notificationHelper.areNotificationsEnabled().value,
          onNotificationStateChange = { enabled ->
            if (enabled) {
              // Check if we need to request permission
              if (!notificationHelper.hasNotificationPermission()) {
                notificationHelper.launch()
              } else {
                notificationHelper.setNotificationsEnabled(true)
              }
            } else {
              notificationHelper.setNotificationsEnabled(false)
            }
          },
          onClose = { settingsBackStack.removeLastOrNull() },
        )
      }
      entry<SettingsRoute.Backup>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        BackupScreen(
          onClose = { settingsBackStack.removeLastOrNull() },
        )
      }
      if (debugModeProvider.isDebugMode()) {
        entry<SettingsRoute.Debug>(
          metadata = BottomSheetSceneStrategy.bottomSheet(),
        ) {
          DebugSettingsScreen(
            triggerNotificationCheck = { debugHelper.triggerImmediateCheck() },
            onClose = { settingsBackStack.removeLastOrNull() },
          )
        }
      }
      entry<SettingsRoute.About>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        val localUriHandler = LocalUriHandler.current

        AboutScreen(
          appVersion = versionProvider.getVersionName(),
          onNavigateToRepo = {
            localUriHandler.openUri("https://github.com/alorma/caducity")
          },
          onClose = { settingsBackStack.removeLastOrNull() },
        )
      }
    },
  )
}
