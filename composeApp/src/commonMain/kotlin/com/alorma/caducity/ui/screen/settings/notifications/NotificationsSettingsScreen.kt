package com.alorma.caducity.ui.screen.settings.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.settings_enable_notifications
import caducity.composeapp.generated.resources.settings_notifications_title
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.notification.NotificationDebugHelper
import com.alorma.caducity.ui.screen.settings.components.CardPosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.screen.settings.previewSettingsModule
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun NotificationsSettingsScreen(
  onBack: () -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
  debugHelper: NotificationDebugHelper = koinInject()
) {
  var notificationsEnabled by remember { mutableStateOf(true) }

  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text(text = stringResource(Res.string.settings_notifications_title)) },
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
        StyledSettingsSwitchCard(
          title = stringResource(Res.string.settings_enable_notifications),
          state = notificationsEnabled,
          position = if (notificationsEnabled) {
            CardPosition.Top
          } else {
            CardPosition.Single
          },
          onCheckedChange = { notificationsEnabled = it },
        )
        if (notificationsEnabled) {
          StyledSettingsCard(
            title = "Test Notification (Debug)",
            subtitle = "Trigger notification check immediately",
            position = CardPosition.Bottom,
            onClick = { debugHelper.triggerImmediateCheck() },
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun NotificationsSettingsScreenPreview() {
  AppPreview(previewSettingsModule) {
    val exitAlwaysScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
      exitDirection = FloatingToolbarExitDirection.Bottom,
    )
    NotificationsSettingsScreen(
      onBack = {},
      scrollConnection = exitAlwaysScrollBehavior,
    )
  }
}