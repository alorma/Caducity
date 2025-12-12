package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.settings_screen_title
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferences
import com.alorma.caducity.ui.theme.supportsDynamicColors
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsSegmented
import com.alorma.compose.settings.ui.SettingsSwitch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SettingsScreen() {
  val themePreferences = koinInject<ThemePreferences>()
  var notificationsEnabled by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(Res.string.settings_screen_title)) },
      )
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues = paddingValues),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      SettingsGroup(
        title = { Text(text = "Appearance") },
      ) {
        SettingsSegmented(
          title = { Text(text = "Theme") },
          selectedItem = themePreferences.loadThemeMode(),
          items = ThemeMode.entries,
          itemTitleMap = { themeMode ->
            when (themeMode) {
              ThemeMode.LIGHT -> "Light"
              ThemeMode.DARK -> "Dark"
              ThemeMode.SYSTEM -> "System"
            }
          },
          onItemSelected = { themePreferences.setThemeModeState(it) },
        )
        if (supportsDynamicColors()) {
          SettingsSwitch(
            title = { Text(text = "Dynamic Colors") },
            state = themePreferences.loadUseDynamicColors(),
            onCheckedChange = { themePreferences.setDynamicColorsEnabled(it) },
          )
        }
      }

      SettingsGroup(
        title = { Text(text = "Notifications") },
      ) {
        SettingsSwitch(
          title = { Text(text = "Enable Notifications") },
          state = notificationsEnabled,
          onCheckedChange = { notificationsEnabled = it },
        )
      }
    }
  }
}
