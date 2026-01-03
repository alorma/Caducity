package com.alorma.caducity.ui.screen.settings.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsButtonGroupCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferences
import com.alorma.caducity.ui.theme.colors.supportsDynamicColors
import org.koin.compose.koinInject

@Composable
fun AppearanceSettingsScreen(
  modifier: Modifier = Modifier,
) {
  val themePreferences = koinInject<ThemePreferences>()

  // Load all string resources at composable level
  val themeLight = stringResource(R.string.settings_theme_light)
  val themeDark = stringResource(R.string.settings_theme_dark)
  val themeSystem = stringResource(R.string.settings_theme_system)

  val colorSchemeVibrant = stringResource(R.string.settings_color_scheme_vibrant)
  val colorSchemeHarmony = stringResource(R.string.settings_color_scheme_harmony)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp)
      .then(modifier),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    StyledSettingsGroup {
      StyledSettingsButtonGroupCard(
        title = stringResource(R.string.settings_theme_title),
        selectedItem = themePreferences.themeMode.value,
        position = ShapePosition.Start,
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
        StyledSettingsSwitchCard(
          title = stringResource(R.string.settings_dynamic_colors),
          state = themePreferences.useDynamicColors.value,
          position = ShapePosition.End,
          onCheckedChange = { themePreferences.setDynamicColorsEnabled(it) },
        )
      }
    }
  }
}
