package com.alorma.caducity.ui.screen.settings.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.settings_enable_notifications
import caducity.composeapp.generated.resources.settings_group_notifications
import caducity.composeapp.generated.resources.settings_notifications_title
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.notification.NotificationDebugHelper
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun NotificationsSettingsScreen(
  onBack: () -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
) {
  val debugHelper = koinInject<NotificationDebugHelper>()
  var notificationsEnabled by remember { mutableStateOf(true) }

  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      TopAppBar(
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
        .padding(paddingValues = paddingValues),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      SettingsGroup(
        title = { Text(text = stringResource(Res.string.settings_group_notifications)) },
      ) {
        SettingsSwitch(
          title = { Text(text = stringResource(Res.string.settings_enable_notifications)) },
          state = notificationsEnabled,
          onCheckedChange = { notificationsEnabled = it },
        )
        if (notificationsEnabled) {
          SettingsMenuLink(
            title = { Text(text = "Test Notification (Debug)") },
            subtitle = { Text(text = "Trigger notification check immediately") },
            onClick = { debugHelper.triggerImmediateCheck() },
          )
        }
      }
    }
  }
}
