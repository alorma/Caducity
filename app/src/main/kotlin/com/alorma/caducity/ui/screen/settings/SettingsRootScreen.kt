package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Backup
import com.alorma.caducity.base.ui.icons.Info
import com.alorma.caducity.base.ui.icons.Notifications
import com.alorma.caducity.base.ui.icons.Palette
import com.alorma.caducity.base.ui.icons.outlined.Settings
import com.alorma.caducity.base.ui.icons.outlined.Shield
import com.alorma.caducity.feature.tracking.SettingsScreen
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun SettingsRootScreen(
  isDebug: Boolean,
  onNavigateToAppearance: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onNavigateToPrivacy: () -> Unit,
  onNavigateToBackup: () -> Unit,
  onNavigateToDebug: () -> Unit,
  onNavigateToAbout: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TrackScreen(screen = SettingsScreen())
  SettingsRootContent(
    isDebug = isDebug,
    onNavigateToAppearance = onNavigateToAppearance,
    onNavigateToNotifications = onNavigateToNotifications,
    onNavigateToPrivacy = onNavigateToPrivacy,
    onNavigateToBackup = onNavigateToBackup,
    onNavigateToDebug = onNavigateToDebug,
    onNavigateToAbout = onNavigateToAbout,
    modifier = modifier,
  )
}

@Composable
private fun SettingsRootContent(
  isDebug: Boolean,
  onNavigateToAppearance: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onNavigateToPrivacy: () -> Unit,
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
      LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        // Group 1: Appearance, Language & Notifications
        item {
          StyledSettingsGroup(
            title = { Text(stringResource(R.string.settings_appearance_title)) },
          ) {
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
              shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
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
              shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
            )
          }
        }

        // Group 2: Privacy & Backup
        item {
          StyledSettingsGroup(
            title = { Text(stringResource(R.string.settings_privacy_title)) },
          ) {
            StyledSettingsCard(
              icon = {
                Icon(
                  imageVector = AppIcons.Outlined.Shield,
                  contentDescription = null,
                )
              },
              title = stringResource(R.string.settings_privacy_title),
              subtitle = stringResource(R.string.settings_privacy_description),
              onClick = onNavigateToPrivacy,
              shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
            )

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
              shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
            )
          }
        }

        // Group 3: Debug (only shown in debug mode)
        if (isDebug) {
          item {
            StyledSettingsGroup(
              title = { Text(stringResource(R.string.settings_debug_title)) },
            ) {
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
                shapes = ListItemDefaults.shapes(),
              )
            }
          }
        }

        // Group 4: About
        item {
          StyledSettingsGroup(
            title = { Text(stringResource(R.string.settings_about_title)) },
          ) {
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
              shapes = ListItemDefaults.shapes(),
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
fun SettingsScreenPreview() {
  PreviewTheme {
    Surface {
      SettingsRootContent(
        isDebug = true,
        onNavigateToAppearance = {},
        onNavigateToNotifications = {},
        onNavigateToPrivacy = {},
        onNavigateToBackup = {},
        onNavigateToDebug = {},
        onNavigateToAbout = {},
      )
    }
  }
}
