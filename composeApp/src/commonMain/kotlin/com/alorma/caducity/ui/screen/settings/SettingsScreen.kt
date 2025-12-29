package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.settings_about_description
import caducity.composeapp.generated.resources.settings_about_title
import caducity.composeapp.generated.resources.settings_appearance_description
import caducity.composeapp.generated.resources.settings_appearance_title
import caducity.composeapp.generated.resources.settings_debug_description
import caducity.composeapp.generated.resources.settings_debug_title
import caducity.composeapp.generated.resources.settings_notifications_description
import caducity.composeapp.generated.resources.settings_notifications_title
import caducity.composeapp.generated.resources.settings_screen_title
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Palette
import com.alorma.caducity.debug.DebugModeProvider
import com.alorma.caducity.ui.screen.settings.components.CardPosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SettingsRootScreen(
  onNavigateToAppearance: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onNavigateToDebug: () -> Unit,
  onNavigateToAbout: () -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
  debugModeProvider: DebugModeProvider = koinInject()
) {
  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = stringResource(Res.string.settings_screen_title),
          )
        },
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
      // Group 1: Appearance & Notifications
      StyledSettingsGroup {
        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Palette,
              contentDescription = null,
            )
          },
          title = stringResource(Res.string.settings_appearance_title),
          subtitle = stringResource(Res.string.settings_appearance_description),
          onClick = onNavigateToAppearance,
          position = CardPosition.Top,
        )

        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Notifications,
              contentDescription = null,
            )
          },
          title = stringResource(Res.string.settings_notifications_title),
          subtitle = stringResource(Res.string.settings_notifications_description),
          onClick = onNavigateToNotifications,
          position = CardPosition.Bottom,
        )
      }

      // Group 2: Debug (only shown in debug mode)
      if (debugModeProvider.isDebugMode()) {
        StyledSettingsGroup {
          StyledSettingsCard(
            icon = {
              Icon(
                imageVector = AppIcons.Settings,
                contentDescription = null,
              )
            },
            title = stringResource(Res.string.settings_debug_title),
            subtitle = stringResource(Res.string.settings_debug_description),
            onClick = onNavigateToDebug,
            position = CardPosition.Single,
          )
        }
      }

      // Group 3: About
      StyledSettingsGroup {
        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Info,
              contentDescription = null,
            )
          },
          title = stringResource(Res.string.settings_about_title),
          subtitle = stringResource(Res.string.settings_about_description),
          onClick = onNavigateToAbout,
          position = CardPosition.Single,
        )
      }
    }
  }
}

