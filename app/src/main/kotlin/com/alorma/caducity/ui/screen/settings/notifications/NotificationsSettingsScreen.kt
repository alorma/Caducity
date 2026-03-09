package com.alorma.caducity.ui.screen.settings.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalTime
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
    notificationTime = notificationHelper.getNotificationTime().value,
    onNotificationTimeChange = { notificationHelper.setNotificationTime(it) },
    onClose = onClose,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsContent(
  areNotificationsEnabled: Boolean,
  onNotificationStateChange: (Boolean) -> Unit,
  areExpiredNotificationsEnabled: Boolean,
  onExpiredNotificationsStateChange: (Boolean) -> Unit,
  areExpiringSoonNotificationsEnabled: Boolean,
  onExpiringSoonNotificationsStateChange: (Boolean) -> Unit,
  notificationTime: LocalTime,
  onNotificationTimeChange: (LocalTime) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showTimePicker by remember { mutableStateOf(false) }

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
          shapes = ListItemDefaults.shapes(),
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
              shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
              onCheckedChange = onExpiredNotificationsStateChange,
            )
            StyledSettingsSwitchCard(
              title = stringResource(R.string.settings_notification_type_expiring),
              state = areExpiringSoonNotificationsEnabled,
              shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
              onCheckedChange = onExpiringSoonNotificationsStateChange,
            )
          }
        }
        item {
          StyledSettingsGroup(
            title = { Text(stringResource(R.string.settings_notification_schedule_title)) }
          ) {
            StyledSettingsCard(
              title = stringResource(R.string.settings_notification_time_title),
              subtitle = "%02d:%02d".format(notificationTime.hour, notificationTime.minute),
              shapes = ListItemDefaults.shapes(),
              onClick = { showTimePicker = true },
            )
          }
        }
      }
      }
    }
  }

  if (showTimePicker) {
    val timePickerState = rememberTimePickerState(
      initialHour = notificationTime.hour,
      initialMinute = notificationTime.minute,
    )
    AlertDialog(
      onDismissRequest = { showTimePicker = false },
      title = { Text(stringResource(R.string.settings_notification_time_dialog_title)) },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          TimePicker(state = timePickerState)
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            onNotificationTimeChange(LocalTime(timePickerState.hour, timePickerState.minute))
            showTimePicker = false
          }
        ) {
          Text(stringResource(R.string.settings_notification_time_confirm))
        }
      },
      dismissButton = {
        TextButton(onClick = { showTimePicker = false }) {
          Text(stringResource(R.string.settings_notification_time_dismiss))
        }
      },
    )
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
        notificationTime = LocalTime(12, 0),
        onNotificationTimeChange = {},
        onClose = {},
      )
    }
  }
}
