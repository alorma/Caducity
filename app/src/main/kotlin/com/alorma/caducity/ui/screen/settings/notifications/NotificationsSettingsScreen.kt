package com.alorma.caducity.ui.screen.settings.notifications

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
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import org.koin.compose.koinInject
import com.alorma.caducity.feature.tracking.NotificationsSettingsScreen as NotificationsSettingsScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen

@Composable
fun NotificationsSettingsScreen(
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  notificationHelper: ExpirationNotificationHelper = koinInject(),
) {
  TrackScreen(screen = NotificationsSettingsScreenEvent())

  notificationHelper.registerContracts()

  NotificationsSettingsContent(
    modifier = modifier,
    areNotificationsEnabled = notificationHelper.areNotificationsEnabled().value,
    onNotificationStateChange = { notificationHelper.changeState(it) },
    areExpiredNotificationsEnabled = notificationHelper.areExpiredNotificationsEnabled().value,
    onExpiredNotificationsStateChange = { notificationHelper.setExpiredNotificationsEnabled(it) },
    areExpiringSoonNotificationsEnabled = notificationHelper.areExpiringSoonNotificationsEnabled().value,
    onExpiringSoonNotificationsStateChange = { notificationHelper.setExpiringSoonNotificationsEnabled(it) },
    onClose = onClose,
  )
}

@Composable
fun NotificationsSettingsContent(
  areNotificationsEnabled: Boolean,
  onNotificationStateChange: (Boolean) -> Unit,
  areExpiredNotificationsEnabled: Boolean,
  onExpiredNotificationsStateChange: (Boolean) -> Unit,
  areExpiringSoonNotificationsEnabled: Boolean,
  onExpiringSoonNotificationsStateChange: (Boolean) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = Modifier.then(modifier),
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = {
          Text(
            text = stringResource(R.string.settings_notifications_title),
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
        StyledSettingsGroup(
          title = { Text(stringResource(R.string.settings_notifications_title)) }
        ) {
        StyledSettingsSwitchCard(
          title = stringResource(R.string.settings_enable_notifications),
          state = areNotificationsEnabled,
          position = ShapePosition.Single,
          onCheckedChange = onNotificationStateChange,
        )
        }
      }
      if (areNotificationsEnabled) {
        item {
          StyledSettingsGroup(
            title = { Text(stringResource(R.string.settings_notification_types_title)) }
          ) {
            StyledSettingsSwitchCard(
              title = stringResource(R.string.settings_notification_type_expired),
              state = areExpiredNotificationsEnabled,
              position = ShapePosition.Start,
              onCheckedChange = onExpiredNotificationsStateChange,
            )
            StyledSettingsSwitchCard(
              title = stringResource(R.string.settings_notification_type_expiring),
              state = areExpiringSoonNotificationsEnabled,
              position = ShapePosition.End,
              onCheckedChange = onExpiringSoonNotificationsStateChange,
            )
          }
        }
      }
      }
    }
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
fun NotificationsSettingsScreenPreview() {
  PreviewTheme {
    Surface {
      NotificationsSettingsContent(
        areNotificationsEnabled = true,
        onNotificationStateChange = {},
        areExpiredNotificationsEnabled = true,
        onExpiredNotificationsStateChange = {},
        areExpiringSoonNotificationsEnabled = true,
        onExpiringSoonNotificationsStateChange = {},
        onClose = {},
      )
    }
  }
}
