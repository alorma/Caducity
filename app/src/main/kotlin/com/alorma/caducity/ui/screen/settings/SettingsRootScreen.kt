package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Backup
import com.alorma.caducity.base.ui.icons.Info
import com.alorma.caducity.base.ui.icons.Notifications
import com.alorma.caducity.base.ui.icons.Palette
import com.alorma.caducity.base.ui.icons.outlined.Settings
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun SettingsRootScreen(
  isDebug: Boolean,
  onNavigateToAppearance: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onNavigateToBackup: () -> Unit,
  onNavigateToDebug: () -> Unit,
  onNavigateToAbout: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = modifier,
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = {
          Text(
            text = stringResource(R.string.settings_screen_title),
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
      // Group 1: Appearance, Language & Notifications
      StyledSettingsGroup {
        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Palette,
              contentDescription = null,
            )
          },
          title = stringResource(R.string.settings_appearance_title),
          subtitle = stringResource(R.string.settings_appearance_description),
          onClick = onNavigateToAppearance,
          position = ShapePosition.Start,
        )

        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Notifications,
              contentDescription = null,
            )
          },
          title = stringResource(R.string.settings_notifications_title),
          subtitle = stringResource(R.string.settings_notifications_description),
          onClick = onNavigateToNotifications,
          position = ShapePosition.End,
        )
      }

      // Group 2: Backup
      StyledSettingsGroup {
        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Backup,
              contentDescription = null,
            )
          },
          title = stringResource(R.string.settings_backup_title),
          subtitle = stringResource(R.string.settings_backup_description),
          onClick = onNavigateToBackup,
          position = ShapePosition.Single,
        )
      }

      // Group 3: Debug (only shown in debug mode)
      if (isDebug) {
        StyledSettingsGroup {
          StyledSettingsCard(
            icon = {
              Icon(
                imageVector = AppIcons.Outlined.Settings,
                contentDescription = null,
              )
            },
            title = stringResource(R.string.settings_debug_title),
            subtitle = stringResource(R.string.settings_debug_description),
            onClick = onNavigateToDebug,
            position = ShapePosition.Single,
          )
        }
      }

      // Group 4: About
      StyledSettingsGroup {
        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Info,
              contentDescription = null,
            )
          },
          title = stringResource(R.string.settings_about_title),
          subtitle = stringResource(R.string.settings_about_description),
          onClick = onNavigateToAbout,
          position = ShapePosition.Single,
        )
      }
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
fun SettingsScreenPreview() {
  PreviewTheme {
    Surface {
      SettingsRootScreen(
        isDebug = true,
        onNavigateToAppearance = {},
        onNavigateToNotifications = {},
        onNavigateToBackup = {},
        onNavigateToDebug = {},
        onNavigateToAbout = {},
      )
    }
  }
}