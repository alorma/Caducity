package com.alorma.caducity.ui.screen.settings.debug

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.alorma.caducity.feature.tracking.DebugRemoteConfigsScreen as DebugRemoteConfigsScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugRemoteConfigsScreen(
  modifier: Modifier = Modifier,
  viewModel: DebugRemoteConfigsViewModel = koinViewModel(),
) {
  TrackScreen(screen = DebugRemoteConfigsScreenEvent())

  val uiState by viewModel.uiState.collectAsState()

  DebugRemoteConfigsContent(
    modifier = modifier,
    uiState = uiState,
  )
}

@Composable
private fun DebugRemoteConfigsContent(
  uiState: DebugRemoteConfigsUiState,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = Modifier.then(modifier),
    snackbarState = rememberAppSnackbarState(),
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = { Text("Remote Configs") },
      )
    },
  ) { paddingValues ->
    ResponsiveSettingsContainer(modifier = Modifier.padding(paddingValues)) {
      StyledSettingsGroup(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = { Text("Remote Configs") },
      ) {
        val entries = uiState.remoteConfigValues.entries.toList()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
          itemsIndexed(entries) { index, (key, configState) ->
            StyledSettingsSwitchCard(
              title = key,
              subtitle =
                if (configState.hasDebugOverride) {
                  "Debug override active"
                } else {
                  "Using default value"
                },
              state = configState.value,
              shapes =
                ListItemDefaults.segmentedShapes(
                  index = index,
                  count = entries.size,
                ),
              onCheckedChange = { enabled ->
                uiState.onToggleRemoteConfig(key, enabled)
              },
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
fun DebugRemoteConfigsScreenPreview() {
  PreviewTheme {
    Surface {
      DebugRemoteConfigsContent(
        uiState =
          DebugRemoteConfigsUiState(
            remoteConfigValues =
              mapOf(
                "feature_new_dashboard" to RemoteConfigUiState(value = true, hasDebugOverride = true),
                "feature_dark_mode" to RemoteConfigUiState(value = false, hasDebugOverride = false),
              ),
            onToggleRemoteConfig = { _, _ -> },
          ),
      )
    }
  }
}
