package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.settings_screen_title
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.ExpirationColorSchemeType
import com.alorma.caducity.base.ui.theme.ThemeMode
import com.alorma.caducity.base.ui.theme.ThemePreferences
import com.alorma.caducity.base.ui.theme.supportsDynamicColors
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsSwitch
import com.alorma.compose.settings.ui.expressive.SettingsButtonGroup
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
) {
  val themePreferences = koinInject<ThemePreferences>()
  var notificationsEnabled by remember { mutableStateOf(true) }

  Scaffold(
    modifier = modifier,
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
        SettingsButtonGroup(
          title = { Text(text = "Theme") },
          selectedItem = themePreferences.themeMode.value,
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
            state = themePreferences.useDynamicColors.value,
            onCheckedChange = { themePreferences.setDynamicColorsEnabled(it) },
          )
        }
      }

      SettingsGroup(
        title = { Text(text = "Expiration Colors") },
      ) {
        SettingsButtonGroup(
          title = { Text(text = "Color Scheme") },
          selectedItem = themePreferences.expirationColorSchemeType.value,
          items = ExpirationColorSchemeType.entries,
          itemTitleMap = { schemeType ->
            when (schemeType) {
              ExpirationColorSchemeType.VIBRANT -> "Vibrant"
              ExpirationColorSchemeType.HARMONIZE -> "Harmonize"
              ExpirationColorSchemeType.GREY -> "Grey"
            }
          },
          onItemSelected = { themePreferences.setExpirationColorSchemeType(it) },
        )
        ExpirationColorLegend()
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

@Composable
private fun ExpirationColorLegend() {
  val expirationColors = CaducityTheme.expirationColorScheme

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    ColorLegendItem(
      label = "Fresh",
      color = expirationColors.fresh,
    )
    ColorLegendItem(
      label = "Expiring Soon",
      color = expirationColors.expiringSoon,
    )
    ColorLegendItem(
      label = "Expired",
      color = expirationColors.expired,
    )
  }
}

@Composable
private fun ColorLegendItem(
  label: String,
  color: Color,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(color),
    )
    Text(text = label)
  }
}
