package com.alorma.caducity.ui.screen.settings.appearance

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import org.koin.compose.koinInject
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsButtonGroupCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.colors.supportsDynamicColors
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.DayOfWeek

@Composable
fun AppearanceSettingsScreen(
  themeMode: ThemeMode,
  onThemeModeChange: (ThemeMode) -> Unit,
  useDynamicTheme: Boolean,
  onUseDynamicTheme: (Boolean) -> Unit,
  firstDayOfWeek: DayOfWeek,
  onFirstDayOfWeekChange: (DayOfWeek) -> Unit,
  modifier: Modifier = Modifier,
  localizedDateFormatter: LocalizedDateFormatter = koinInject(),
) {
  AppScaffold(
    modifier = Modifier.then(modifier),
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = {
          Text(
            text = stringResource(R.string.settings_appearance_title),
          )
        },
      )
    },
  ) { paddingValues ->
    ResponsiveSettingsContainer(modifier = Modifier.padding(paddingValues)) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
      StyledSettingsGroup {

        // Load all string resources at composable level
        val themeLight = stringResource(R.string.settings_theme_light)
        val themeDark = stringResource(R.string.settings_theme_dark)
        val themeSystem = stringResource(R.string.settings_theme_system)

        StyledSettingsButtonGroupCard(
          title = stringResource(R.string.settings_theme_title),
          selectedItem = themeMode,
          position = ShapePosition.Start,
          items = ThemeMode.entries,
          itemTitleMap = { themeMode ->
            when (themeMode) {
              ThemeMode.LIGHT -> themeLight
              ThemeMode.DARK -> themeDark
              ThemeMode.SYSTEM -> themeSystem
            }
          },
          onItemSelected = { onThemeModeChange(it) },
        )
        if (supportsDynamicColors()) {
          StyledSettingsSwitchCard(
            title = stringResource(R.string.settings_dynamic_colors),
            state = useDynamicTheme,
            position = ShapePosition.End,
            onCheckedChange = { onUseDynamicTheme(it) },
          )
        }
      }

      // Calendar settings group
      StyledSettingsGroup {
        StyledSettingsButtonGroupCard(
          title = stringResource(R.string.settings_first_day_of_week_title),
          selectedItem = firstDayOfWeek,
          position = ShapePosition.Single,
          items = listOf(DayOfWeek.MONDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
          itemTitleMap = { day ->
            localizedDateFormatter.getDayOfWeekFullName(day)
          },
          onItemSelected = { onFirstDayOfWeekChange(it) },
        )
      }
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
fun AppearanceSettingsScreenPreview() {
  PreviewTheme {
    Surface {
      val themeMode = if (isSystemInDarkTheme()) {
        ThemeMode.DARK
      } else {
        ThemeMode.LIGHT
      }
      AppearanceSettingsScreen(
        themeMode = themeMode,
        useDynamicTheme = true,
        firstDayOfWeek = DayOfWeek.MONDAY,
        onThemeModeChange = {},
        onUseDynamicTheme = {},
        onFirstDayOfWeekChange = {},
      )
    }
  }
}