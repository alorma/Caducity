package com.alorma.caducity.ui.screen.settings.notifications

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
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import org.koin.compose.koinInject

@Composable
fun NotificationsSettingsScreen(
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  notificationHelper: ExpirationNotificationHelper = koinInject(),
) {

  notificationHelper.registerContracts()

  NotificationsSettingsContent(
    modifier = modifier,
    areNotificationsEnabled = notificationHelper.areNotificationsEnabled().value,
    onNotificationStateChange = { notificationHelper.changeState(it) },
    onClose = onClose,
  )
}

@Composable
fun NotificationsSettingsContent(
  areNotificationsEnabled: Boolean,
  onNotificationStateChange: (Boolean) -> Unit,
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
          title = stringResource(R.string.settings_enable_notifications),
          state = areNotificationsEnabled,
          position = ShapePosition.Single,
          onCheckedChange = onNotificationStateChange,
        )
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
fun NotificationsSettingsScreenPreview() {
  PreviewTheme {
    Surface {
      NotificationsSettingsContent(
        areNotificationsEnabled = true,
        onNotificationStateChange = {},
        onClose = {},
      )
    }
  }
}