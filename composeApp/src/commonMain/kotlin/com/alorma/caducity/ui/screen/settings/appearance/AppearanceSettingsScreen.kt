package com.alorma.caducity.ui.screen.settings.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.settings_appearance_title
import caducity.composeapp.generated.resources.settings_color_scheme_harmony
import caducity.composeapp.generated.resources.settings_color_scheme_title
import caducity.composeapp.generated.resources.settings_color_scheme_vibrant
import caducity.composeapp.generated.resources.settings_dynamic_colors
import caducity.composeapp.generated.resources.settings_expiration_legend_expired
import caducity.composeapp.generated.resources.settings_expiration_legend_expiring_soon
import caducity.composeapp.generated.resources.settings_expiration_legend_fresh
import caducity.composeapp.generated.resources.settings_theme_dark
import caducity.composeapp.generated.resources.settings_theme_light
import caducity.composeapp.generated.resources.settings_theme_system
import caducity.composeapp.generated.resources.settings_theme_title
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.ExpirationColorSchemeType
import com.alorma.caducity.base.ui.theme.ThemeMode
import com.alorma.caducity.base.ui.theme.ThemePreferences
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.base.ui.theme.supportsDynamicColors
import com.alorma.caducity.ui.screen.settings.components.CardPosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsButtonGroupCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.screen.settings.previewSettingsModule
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AppearanceSettingsScreen(
  onBack: () -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
) {
  val themePreferences = koinInject<ThemePreferences>()

  // Load all string resources at composable level
  val themeLight = stringResource(Res.string.settings_theme_light)
  val themeDark = stringResource(Res.string.settings_theme_dark)
  val themeSystem = stringResource(Res.string.settings_theme_system)

  val colorSchemeVibrant = stringResource(Res.string.settings_color_scheme_vibrant)
  val colorSchemeHarmony = stringResource(Res.string.settings_color_scheme_harmony)

  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text(text = stringResource(Res.string.settings_appearance_title)) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = AppIcons.Back,
              contentDescription = "Back"
            )
          }
        }
      )
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      StyledSettingsGroup {
        StyledSettingsButtonGroupCard(
          title = stringResource(Res.string.settings_theme_title),
          selectedItem = themePreferences.themeMode.value,
          position = CardPosition.Top,
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
            title = stringResource(Res.string.settings_dynamic_colors),
            state = themePreferences.useDynamicColors.value,
            position = CardPosition.Bottom,
            onCheckedChange = { themePreferences.setDynamicColorsEnabled(it) },
          )
        }
      }

      StyledSettingsButtonGroupCard(
        title = stringResource(Res.string.settings_color_scheme_title),
        selectedItem = themePreferences.expirationColorSchemeType.value,
        items = ExpirationColorSchemeType.entries,
        position = CardPosition.Single,
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

@Preview
@Composable
private fun AppearanceSettingsScreenPreview() {
  AppPreview(previewSettingsModule) {
    val exitAlwaysScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
      exitDirection = FloatingToolbarExitDirection.Bottom,
    )
    AppearanceSettingsScreen(
      onBack = {},
      scrollConnection = exitAlwaysScrollBehavior,
    )
  }
}