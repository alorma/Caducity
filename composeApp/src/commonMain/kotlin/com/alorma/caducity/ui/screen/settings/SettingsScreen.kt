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
import caducity.composeapp.generated.resources.settings_color_scheme_harmony
import caducity.composeapp.generated.resources.settings_color_scheme_title
import caducity.composeapp.generated.resources.settings_color_scheme_vibrant
import caducity.composeapp.generated.resources.settings_dynamic_colors
import caducity.composeapp.generated.resources.settings_enable_notifications
import caducity.composeapp.generated.resources.settings_expiration_legend_expired
import caducity.composeapp.generated.resources.settings_expiration_legend_expiring_soon
import caducity.composeapp.generated.resources.settings_expiration_legend_fresh
import caducity.composeapp.generated.resources.settings_group_appearance
import caducity.composeapp.generated.resources.settings_group_expiration_colors
import caducity.composeapp.generated.resources.settings_group_notifications
import caducity.composeapp.generated.resources.settings_screen_title
import caducity.composeapp.generated.resources.settings_theme_dark
import caducity.composeapp.generated.resources.settings_theme_light
import caducity.composeapp.generated.resources.settings_theme_system
import caducity.composeapp.generated.resources.settings_theme_title
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.ExpirationColorSchemeType
import com.alorma.caducity.base.ui.theme.ThemeMode
import com.alorma.caducity.base.ui.theme.ThemePreferences
import com.alorma.caducity.base.ui.theme.supportsDynamicColors
import com.alorma.caducity.notification.NotificationDebugHelper
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.alorma.compose.settings.ui.expressive.SettingsButtonGroup
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
) {
  val themePreferences = koinInject<ThemePreferences>()
  val debugHelper = koinInject<NotificationDebugHelper>()
  var notificationsEnabled by remember { mutableStateOf(true) }

  // Load all string resources at composable level
  val themeLight = stringResource(Res.string.settings_theme_light)
  val themeDark = stringResource(Res.string.settings_theme_dark)
  val themeSystem = stringResource(Res.string.settings_theme_system)

  val colorSchemeVibrant = stringResource(Res.string.settings_color_scheme_vibrant)
  val colorSchemeHarmony = stringResource(Res.string.settings_color_scheme_harmony)

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
        title = { Text(text = stringResource(Res.string.settings_group_appearance)) },
      ) {
        SettingsButtonGroup(
          title = { Text(text = stringResource(Res.string.settings_theme_title)) },
          selectedItem = themePreferences.themeMode.value,
          items = ThemeMode.entries,
          itemTitleMap = { themeMode ->
            when (themeMode) {
              ThemeMode.LIGHT -> themeLight
              ThemeMode.DARK -> themeDark
              ThemeMode.SYSTEM -> themeSystem
            }
          },
          onItemSelected = { themePreferences.setThemeModeState(it) },
        )
        if (supportsDynamicColors()) {
          SettingsSwitch(
            title = { Text(text = stringResource(Res.string.settings_dynamic_colors)) },
            state = themePreferences.useDynamicColors.value,
            onCheckedChange = { themePreferences.setDynamicColorsEnabled(it) },
          )
        }
      }

      SettingsGroup(
        title = { Text(text = stringResource(Res.string.settings_group_expiration_colors)) },
      ) {
        SettingsButtonGroup(
          title = { Text(text = stringResource(Res.string.settings_color_scheme_title)) },
          selectedItem = themePreferences.expirationColorSchemeType.value,
          items = ExpirationColorSchemeType.entries,
          itemTitleMap = { schemeType ->
            when (schemeType) {
              ExpirationColorSchemeType.VIBRANT -> colorSchemeVibrant
              ExpirationColorSchemeType.HARMONIZE -> colorSchemeHarmony
            }
          },
          onItemSelected = { themePreferences.setExpirationColorSchemeType(it) },
        )
        ExpirationColorLegend()
      }

      SettingsGroup(
        title = { Text(text = stringResource(Res.string.settings_group_notifications)) },
      ) {
        SettingsSwitch(
          title = { Text(text = stringResource(Res.string.settings_enable_notifications)) },
          state = notificationsEnabled,
          onCheckedChange = { notificationsEnabled = it },
        )
        SettingsMenuLink(
          title = { Text(text = "Test Notification (Debug)") },
          subtitle = { Text(text = "Trigger notification check immediately") },
          onClick = { debugHelper.triggerImmediateCheck() },
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
      label = stringResource(Res.string.settings_expiration_legend_fresh),
      color = expirationColors.fresh,
    )
    ColorLegendItem(
      label = stringResource(Res.string.settings_expiration_legend_expiring_soon),
      color = expirationColors.expiringSoon,
    )
    ColorLegendItem(
      label = stringResource(Res.string.settings_expiration_legend_expired),
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
