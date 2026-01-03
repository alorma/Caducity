package com.alorma.caducity.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.ui.screen.settings.about.AboutScreen
import com.alorma.caducity.ui.screen.settings.appearance.AppearanceSettingsScreen
import com.alorma.caducity.ui.screen.settings.backup.BackupScreen
import com.alorma.caducity.ui.screen.settings.debug.DebugSettingsScreen
import com.alorma.caducity.ui.screen.settings.language.LanguageSettingsScreen
import com.alorma.caducity.ui.screen.settings.notifications.NotificationsSettingsScreen
import org.koin.compose.koinInject

@Composable
fun SettingsContainer(
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
  debugModeProvider: DebugModeProvider = koinInject()
) {
  val settingsBackStack = retain {
    mutableStateListOf<NavKey>(SettingsRoute.Root)
  }

  val bottomSheetStrategy = remember {
    BottomSheetSceneStrategy<NavKey>()
  }

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
          onNavigateToAppearance = { settingsBackStack.add(SettingsRoute.Appearance) },
          onNavigateToLanguage = { settingsBackStack.add(SettingsRoute.Language) },
          onNavigateToNotifications = { settingsBackStack.add(SettingsRoute.Notifications) },
          onNavigateToBackup = { settingsBackStack.add(SettingsRoute.Backup) },
          onNavigateToDebug = { settingsBackStack.add(SettingsRoute.Debug) },
          onNavigateToAbout = { settingsBackStack.add(SettingsRoute.About) },
        )
      }
      entry<SettingsRoute.Appearance>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        AppearanceSettingsScreen()
      }
      entry<SettingsRoute.Language>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        LanguageSettingsScreen()
      }
      entry<SettingsRoute.Notifications>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        NotificationsSettingsScreen()
      }
      entry<SettingsRoute.Backup>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        BackupScreen()
      }
      if (debugModeProvider.isDebugMode()) {
        entry<SettingsRoute.Debug>(
          metadata = BottomSheetSceneStrategy.bottomSheet(),
        ) {
          DebugSettingsScreen()
        }
      }
      entry<SettingsRoute.About>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
      ) {
        AboutScreen()
      }
    },
  )
}
