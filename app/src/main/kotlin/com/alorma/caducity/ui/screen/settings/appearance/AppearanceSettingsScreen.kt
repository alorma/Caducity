package com.alorma.caducity.ui.screen.settings.appearance

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.feature.tracking.AppearanceSettingsScreen as AppearanceSettingsScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsButtonGroupCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.colors.supportsDynamicColors
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.DayOfWeek
import org.koin.compose.koinInject

@Composable
fun AppearanceSettingsScreen(
  themeMode: ThemeMode,
  onThemeModeChange: (ThemeMode) -> Unit,
  useDynamicTheme: Boolean,
  onUseDynamicTheme: (Boolean) -> Unit,
  themeTone: com.alorma.caducity.ui.theme.ThemeTone,
  onThemeToneChange: (com.alorma.caducity.ui.theme.ThemeTone) -> Unit,
  firstDayOfWeek: DayOfWeek,
  onFirstDayOfWeekChange: (DayOfWeek) -> Unit,
  modifier: Modifier = Modifier,
  localizedDateFormatter: LocalizedDateFormatter = koinInject(),
) {
  TrackScreen(screen = AppearanceSettingsScreenEvent())
  AppearanceSettingsContent(
    themeMode = themeMode,
    onThemeModeChange = onThemeModeChange,
    useDynamicTheme = useDynamicTheme,
    onUseDynamicTheme = onUseDynamicTheme,
    themeTone = themeTone,
    onThemeToneChange = onThemeToneChange,
    firstDayOfWeek = firstDayOfWeek,
    onFirstDayOfWeekChange = onFirstDayOfWeekChange,
    modifier = modifier,
    localizedDateFormatter = localizedDateFormatter,
  )
}

@Composable
private fun AppearanceSettingsContent(
  themeMode: ThemeMode,
  onThemeModeChange: (ThemeMode) -> Unit,
  useDynamicTheme: Boolean,
  onUseDynamicTheme: (Boolean) -> Unit,
  themeTone: com.alorma.caducity.ui.theme.ThemeTone,
  onThemeToneChange: (com.alorma.caducity.ui.theme.ThemeTone) -> Unit,
  firstDayOfWeek: DayOfWeek,
  onFirstDayOfWeekChange: (DayOfWeek) -> Unit,
  modifier: Modifier = Modifier,
  localizedDateFormatter: LocalizedDateFormatter,
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
      LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        item {
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
        }

        // Tone settings group
        item {
          StyledSettingsGroup {
            // Load all string resources at composable level
            val toneVibrant = stringResource(R.string.settings_tone_vibrant)
            val toneSoft = stringResource(R.string.settings_tone_soft)

            StyledSettingsButtonGroupCard(
              title = stringResource(R.string.settings_tone_title),
              selectedItem = themeTone,
              position = ShapePosition.Single,
              items = com.alorma.caducity.ui.theme.ThemeTone.entries,
              itemTitleMap = { tone ->
                when (tone) {
                  com.alorma.caducity.ui.theme.ThemeTone.VIBRANT -> toneVibrant
                  com.alorma.caducity.ui.theme.ThemeTone.SOFT -> toneSoft
                }
              },
              onItemSelected = { onThemeToneChange(it) },
            )
          }
        }

        // Calendar settings group
        item {
          StyledSettingsGroup(
            title = { Text(stringResource(R.string.settings_calendar_options)) }
          ) {
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
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
fun AppearanceSettingsScreenPreview() {
  PreviewTheme {
    Surface {
      val themeMode = if (isSystemInDarkTheme()) {
        ThemeMode.DARK
      } else {
        ThemeMode.LIGHT
      }
      AppearanceSettingsContent(
        themeMode = themeMode,
        useDynamicTheme = true,
        themeTone = com.alorma.caducity.ui.theme.ThemeTone.VIBRANT,
        localizedDateFormatter = LocalizedDateFormatter(),
        firstDayOfWeek = DayOfWeek.MONDAY,
        onThemeModeChange = {},
        onUseDynamicTheme = {},
        onThemeToneChange = {},
        onFirstDayOfWeekChange = {},
      )
    }
  }
}
