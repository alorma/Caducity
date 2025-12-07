package com.alorma.caducity.settings

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
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsSwitch

@Composable
fun SettingsScreen() {
  var darkModeEnabled by remember { mutableStateOf(false) }
  var dynamicColorsEnabled by remember { mutableStateOf(false) }
  var notificationsEnabled by remember { mutableStateOf(true) }

  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
  ) {
    SettingsGroup(
      title = { Text(text = "Appearance") },
    ) {
      SettingsSwitch(
        title = { Text(text = "Dark Mode") },
        subtitle = { Text(text = "Use dark theme") },
        state = darkModeEnabled,
        onCheckedChange = { darkModeEnabled = it },
      )
      SettingsSwitch(
        title = { Text(text = "Dynamic Colors") },
        subtitle = { Text(text = "Use Material You dynamic colors") },
        state = dynamicColorsEnabled,
        onCheckedChange = { dynamicColorsEnabled = it },
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
}
