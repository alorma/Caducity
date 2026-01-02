package com.alorma.caducity.ui.screen.settings.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.theme.preview.AppPreview
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.screen.settings.previewSettingsModule
import androidx.compose.ui.res.stringResource
import org.koin.compose.koinInject

@Composable
fun NotificationsSettingsScreen(
  modifier: Modifier = Modifier,
  notificationHelper: ExpirationNotificationHelper = koinInject(),
) {

  notificationHelper.registerContract()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp)
      .then(modifier),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    StyledSettingsGroup {
      StyledSettingsSwitchCard(
        title = stringResource(R.string.settings_enable_notifications),
        state = notificationHelper.areNotificationsEnabled().value,
        position = ShapePosition.Single,
        onCheckedChange = { enabled ->
          if (enabled) {
            // Check if we need to request permission
            if (!notificationHelper.hasNotificationPermission()) {
              notificationHelper.launch()
            } else {
              notificationHelper.setNotificationsEnabled(true)
            }
          } else {
            notificationHelper.setNotificationsEnabled(false)
          }
        },
      )
    }
  }
}

@Preview
@Composable
private fun NotificationsSettingsScreenPreview() {
  AppPreview(previewSettingsModule) {
    NotificationsSettingsScreen()
  }
}