package com.alorma.caducity.ui.screen.settings.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.config.remoteconfig.rememberRemoteConfig
import com.alorma.caducity.feature.tracking.DebugSettingsScreen as DebugSettingsScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCheckboxCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.transform.TransformFeature
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugSettingsScreen(
  onNavigateToRemoteConfigs: () -> Unit,
  onNavigateToTransform: (itemIds: List<String>) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DebugSettingsViewModel = koinViewModel(),
) {
  TrackScreen(screen = DebugSettingsScreenEvent())

  val uiState by viewModel.uiState.collectAsState()
  val snackbarState = rememberAppSnackbarState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        DebugSettingsSideEffect.FakeDataPopulated -> {
          coroutineScope.launch {
            snackbarState.showSnackbar(
              message = R.string.debug_fake_data_success,
              type = AppFeedbackType.Success,
            )
          }
        }

        DebugSettingsSideEffect.Error -> {
          coroutineScope.launch {
            snackbarState.showSnackbar(
              message = R.string.debug_error,
              type = AppFeedbackType.Error,
            )
          }
        }

        is DebugSettingsSideEffect.OpenTransform -> onNavigateToTransform(effect.itemIds)
      }
    }
  }

  DebugSettingsContent(
    modifier = modifier,
    uiState = uiState,
    snackbarState = snackbarState,
    showTransform = rememberRemoteConfig<TransformFeature>().isEnabled(),
    onNavigateToRemoteConfigs = onNavigateToRemoteConfigs,
  )
}

@Composable
private fun DebugSettingsContent(
  uiState: DebugSettingsUiState,
  snackbarState: AppSnackbarState,
  showTransform: Boolean,
  onNavigateToRemoteConfigs: () -> Unit,
  modifier: Modifier = Modifier,
) {
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
    ResponsiveSettingsContainer(modifier = Modifier.padding(paddingValues)) {
      LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        // Data Generation Group
        item {
          StyledSettingsGroup(
            title = { Text("Data Generation") },
          ) {
            StyledSettingsCard(
              title = "Populate Fake Data",
              subtitle =
                if (uiState.isGenerating) {
                  "Generating test data..."
                } else {
                  "Clear all data and create test items with all statuses"
                },
              shapes = ListItemDefaults.shapes(),
              onClick = { uiState.onPopulateFakeData() },
              enabled = !uiState.isGenerating,
            )
          }
        }

        // Transforma Group (behind the transform_enabled remote config)
        if (showTransform) {
          item {
            StyledSettingsGroup(
              title = { Text("Transforma") },
            ) {
              StyledSettingsCard(
                title = "Open Transforma",
                subtitle = "With the soonest unit of up to three products",
                shapes = ListItemDefaults.shapes(),
                onClick = { uiState.onOpenTransform() },
              )
              StyledSettingsCard(
                title = "Open Transforma (free entry)",
                subtitle = "Without ingredients",
                shapes = ListItemDefaults.shapes(),
                onClick = { uiState.onOpenTransformFree() },
              )
            }
          }
        }

        // Notifications Group
        item {
          StyledSettingsGroup(
            title = { Text("Notifications") },
          ) {
            StyledSettingsCard(
              title = "Test Notification",
              subtitle = "Trigger notification check immediately",
              shapes = ListItemDefaults.shapes(),
              onClick = { uiState.onTriggerNotificationCheck() },
            )
          }
        }

        // Remote Config Group
        item {
          StyledSettingsGroup(
            title = { Text("Remote Config") },
          ) {
            StyledSettingsCard(
              title = "Remote Configs",
              subtitle = "View and toggle remote config values",
              shapes = ListItemDefaults.shapes(),
              onClick = onNavigateToRemoteConfigs,
            )
          }
        }

        // Consent Settings Group (Debug Only)
        item {
          StyledSettingsGroup(
            title = {
              Text(
                text = stringResource(R.string.debug_consent_section_title),
              )
            },
          ) {
            StyledSettingsCheckboxCard(
              title = stringResource(R.string.debug_consent_ad_storage_title),
              subtitle = stringResource(R.string.debug_consent_ad_storage_description),
              state = uiState.adStorageEnabled,
              shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
              onCheckedChange = { uiState.onToggleAdStorage(it) },
            )

            StyledSettingsCheckboxCard(
              title = stringResource(R.string.debug_consent_ad_user_data_title),
              subtitle = stringResource(R.string.debug_consent_ad_user_data_description),
              state = uiState.adUserDataEnabled,
              shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
              onCheckedChange = { uiState.onToggleAdUserData(it) },
            )

            StyledSettingsCheckboxCard(
              title = stringResource(R.string.debug_consent_ad_personalization_title),
              subtitle = stringResource(R.string.debug_consent_ad_personalization_description),
              state = uiState.adPersonalizationEnabled,
              shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
              onCheckedChange = { uiState.onToggleAdPersonalization(it) },
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
fun DebugSettingsScreenPreview() {
  PreviewTheme {
    Surface {
      DebugSettingsContent(
        uiState =
          DebugSettingsUiState(
            isGenerating = true,
            adStorageEnabled = true,
            adUserDataEnabled = true,
            adPersonalizationEnabled = true,
            onPopulateFakeData = {},
            onOpenTransform = {},
            onOpenTransformFree = {},
            onTriggerNotificationCheck = {},
            onToggleAdStorage = {},
            onToggleAdUserData = {},
            onToggleAdPersonalization = {},
          ),
        snackbarState = rememberAppSnackbarState(),
        showTransform = true,
        onNavigateToRemoteConfigs = {},
      )
    }
  }
}
