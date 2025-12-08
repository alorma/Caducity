package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferences
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import org.koin.compose.koinInject

@Composable
fun SettingsScreen() {
  val themePreferences = koinInject<ThemePreferences>()
  var notificationsEnabled by remember { mutableStateOf(true) }
  var showThemeDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
  ) {
    SettingsGroup(
      title = { Text(text = "Appearance") },
    ) {
      SettingsMenuLink(
        title = { Text(text = "Theme") },
        subtitle = { 
          Text(text = when (themePreferences.themeMode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.SYSTEM -> "System default"
          })
        },
        onClick = { showThemeDialog = true },
      )
      SettingsSwitch(
        title = { Text(text = "Dynamic Colors") },
        subtitle = { Text(text = "Use Material You dynamic colors") },
        state = themePreferences.useDynamicColors,
        onCheckedChange = { themePreferences.setUseDynamicColors(it) },
      )
    }

    SettingsGroup(
      title = { Text(text = "Notifications") },
    ) {
      SettingsSwitch(
        title = { Text(text = "Enable Notifications") },
        subtitle = { Text(text = "Receive push notifications") },
        state = notificationsEnabled,
        onCheckedChange = { notificationsEnabled = it },
      )
    }
  }

  if (showThemeDialog) {
    ThemeSelectionDialog(
      currentTheme = themePreferences.themeMode,
      onThemeSelected = { mode ->
        themePreferences.setThemeMode(mode)
        showThemeDialog = false
      },
      onDismiss = { showThemeDialog = false },
    )
  }
}
