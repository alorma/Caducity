package com.alorma.caducity.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.alorma.caducity.feature.notification.NotificationDebugHelper
import com.alorma.caducity.ui.screen.settings.about.AboutScreen
import com.alorma.caducity.ui.screen.settings.appearance.AppearanceSettingsScreen
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import com.alorma.caducity.ui.screen.settings.backup.BackupScreen
import com.alorma.caducity.ui.screen.settings.debug.DebugSettingsScreen
import com.alorma.caducity.ui.screen.settings.notifications.NotificationsSettingsScreen
import com.alorma.caducity.ui.screen.settings.privacy.PrivacySettingsScreen
import com.alorma.caducity.ui.theme.ThemePreferences
import org.koin.compose.koinInject

@Composable
fun SettingsContainer(
  modifier: Modifier = Modifier,
  themePreferences: ThemePreferences = koinInject(),
  calendarPreferences: CalendarPreferences = koinInject(),
  debugModeProvider: DebugModeProvider = koinInject(),
  debugHelper: NotificationDebugHelper = koinInject(),
  versionProvider: AppVersionProvider = koinInject(),
) {
  val settingsBackStack = retain {
    mutableStateListOf<NavKey>(SettingsRoute.Root)
  }

  NavDisplay(
    modifier = modifier,
    backStack = settingsBackStack,
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = entryProvider {
      entry<SettingsRoute.Root> {
        SettingsRootScreen(
          isDebug = debugModeProvider.isDebugMode(),
          onNavigateToAppearance = { settingsBackStack.add(SettingsRoute.Appearance) },
          onNavigateToNotifications = { settingsBackStack.add(SettingsRoute.Notifications) },
          onNavigateToPrivacy = { settingsBackStack.add(SettingsRoute.Privacy) },
          onNavigateToBackup = { settingsBackStack.add(SettingsRoute.Backup) },
          onNavigateToDebug = { settingsBackStack.add(SettingsRoute.Debug) },
          onNavigateToAbout = { settingsBackStack.add(SettingsRoute.About) },
        )
      }
      entry<SettingsRoute.Appearance> {
        val calendarConfigState by calendarPreferences.state.collectAsState()

        AppearanceSettingsScreen(
          themeMode = themePreferences.themeMode.value,
          useDynamicTheme = themePreferences.useDynamicColors.value,
          themeTone = themePreferences.themeTone.value,
          firstDayOfWeek = calendarConfigState.firstDayOfWeek,
          onThemeModeChange = { themePreferences.setThemeModeState(it) },
          onUseDynamicTheme = { themePreferences.setDynamicColorsEnabled(it) },
          onThemeToneChange = { themePreferences.setThemeTone(it) },
          onFirstDayOfWeekChange = { calendarPreferences.setFirstDayOfWeek(it) },
        )
      }
      entry<SettingsRoute.Notifications> {
        NotificationsSettingsScreen(
          onClose = { settingsBackStack.removeLastOrNull() },
        )
      }
      entry<SettingsRoute.Privacy> {
        PrivacySettingsScreen(
          onClose = { settingsBackStack.removeLastOrNull() },
        )
      }
      entry<SettingsRoute.Backup> {
        BackupScreen()
      }
      if (debugModeProvider.isDebugMode()) {
        entry<SettingsRoute.Debug> {
          DebugSettingsScreen()
        }
      }
      entry<SettingsRoute.About> {
        val localUriHandler = LocalUriHandler.current

        AboutScreen(
          appVersion = versionProvider.getVersionName(),
          onNavigateToRepo = {
            localUriHandler.openUri("https://github.com/alorma/caducity")
          },
        )
      }
    },
  )
}
