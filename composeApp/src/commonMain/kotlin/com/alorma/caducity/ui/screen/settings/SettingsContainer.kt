package com.alorma.caducity.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.ui.screen.settings.about.AboutScreen
import com.alorma.caducity.ui.screen.settings.appearance.AppearanceSettingsScreen
import com.alorma.caducity.ui.screen.settings.notifications.NotificationsSettingsScreen

@Composable
fun SettingsContainer(
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
) {
  val settingsBackStack = remember {
    mutableStateListOf<NavKey>(SettingsRoute.Root)
  }

  NavDisplay(
    modifier = modifier,
    backStack = settingsBackStack,
    onBack = {
      if (settingsBackStack.size > 1) {
        settingsBackStack.removeLast()
      }
    },
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = entryProvider {
      entry<SettingsRoute.Root> {
        SettingsRootScreen(
          scrollConnection = scrollConnection,
          onNavigateToAppearance = { settingsBackStack.add(SettingsRoute.Appearance) },
          onNavigateToNotifications = { settingsBackStack.add(SettingsRoute.Notifications) },
          onNavigateToAbout = { settingsBackStack.add(SettingsRoute.About) },
        )
      }
      entry<SettingsRoute.Appearance> {
        AppearanceSettingsScreen(
          scrollConnection = scrollConnection,
          onBack = { settingsBackStack.removeLast() },
        )
      }
      entry<SettingsRoute.Notifications> {
        NotificationsSettingsScreen(
          scrollConnection = scrollConnection,
          onBack = { settingsBackStack.removeLast() },
        )
      }
      entry<SettingsRoute.About> {
        AboutScreen(
          scrollConnection = scrollConnection,
          onBack = { settingsBackStack.removeLast() },
        )
      }
    },
  )
}
