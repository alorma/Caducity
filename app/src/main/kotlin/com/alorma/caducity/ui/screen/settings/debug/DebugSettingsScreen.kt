package com.alorma.caducity.ui.screen.settings.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: DebugSettingsViewModel = koinViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val snackbarState = rememberAppSnackbarState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
        DebugSettingsSideEffect.FakeDataPopulated -> {
          coroutineScope.launch {
            snackbarState.showSnackbar(
              message = R.string.debug_fake_data_success,
              type = AppFeedbackType.Success,
            )
          }
        }
        is DebugSettingsSideEffect.RemoteConfigRefreshed -> {
          coroutineScope.launch {
            val message = if (effect.activated) {
              "Remote Config refreshed with new values"
            } else {
              "Remote Config refreshed (no new values)"
            }
            snackbarState.showSnackbar(
              message = message,
              type = AppFeedbackType.Success,
            )
          }
        }
      }
    }
  }
  AppScaffold(
    modifier = Modifier.then(modifier),
    snackbarState = snackbarState,
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = {
          Text(
            text = stringResource(R.string.settings_debug_title),
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
      // Data Generation Group
      StyledSettingsGroup {
        StyledSettingsCard(
          title = "Populate Fake Data",
          subtitle = if (uiState.isGenerating) {
            "Generating test data..."
          } else {
            "Clear all data and create test items with all statuses"
          },
          position = ShapePosition.Single,
          onClick = { viewModel.onPopulateFakeData() },
          enabled = !uiState.isGenerating,
        )
      }

      // Notifications Group
      StyledSettingsGroup {
        StyledSettingsCard(
          title = "Test Notification",
          subtitle = "Trigger notification check immediately",
          position = ShapePosition.Single,
          onClick = { viewModel.onTriggerNotificationCheck() },
        )
      }

      // Remote Config Group
      StyledSettingsGroup {
        StyledSettingsCard(
          title = "Refresh Remote Config",
          subtitle = if (uiState.isRefreshingRemoteConfig) {
            "Fetching latest config..."
          } else {
            "Fetch and activate latest config values"
          },
          position = ShapePosition.Single,
          onClick = { viewModel.onRefreshRemoteConfig() },
          enabled = !uiState.isRefreshingRemoteConfig,
        )
      }
      
      // Remote Configs Override Group
      if (uiState.remoteConfigValues.isNotEmpty()) {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Remote Configs",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
          )
          
          StyledSettingsGroup {
            uiState.remoteConfigValues.entries.forEachIndexed { index, (key, configState) ->
              val position = when {
                uiState.remoteConfigValues.size == 1 -> ShapePosition.Single
                index == 0 -> ShapePosition.Start
                index == uiState.remoteConfigValues.size - 1 -> ShapePosition.End
                else -> ShapePosition.Middle
              }
              
              StyledSettingsSwitchCard(
                title = key,
                subtitle = if (configState.hasDebugOverride) {
                  "Debug override active"
                } else {
                  "Using default value"
                },
                state = configState.value,
                position = position,
                onCheckedChange = { enabled ->
                  viewModel.onToggleRemoteConfig(key, enabled)
                },
              )
            }
          }
        }
      }
    }
  }

  // Error dialog
  uiState.error?.let { error ->
    AlertDialog(
      onDismissRequest = { viewModel.dismissError() },
      title = { Text("Generation Failed") },
      text = { Text(error) },
      confirmButton = {
        TextButton(onClick = { viewModel.dismissError() }) {
          Text("OK")
        }
      }
    )
  }
}


@PreviewDynamicLightDark
@Composable
fun DebugSettingsScreenPreview() {
  PreviewTheme {
    Surface {
      DebugSettingsScreen()
    }
  }
}