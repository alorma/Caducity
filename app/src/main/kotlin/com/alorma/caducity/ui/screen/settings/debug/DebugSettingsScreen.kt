package com.alorma.caducity.ui.screen.settings.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: DebugSettingsViewModel = koinViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
        else -> {}
      }
    }
  }
  AppScaffold(
    modifier = Modifier.then(modifier),
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
      // Notifications Group
      StyledSettingsGroup {
        StyledSettingsCard(
          title = "Test Notification",
          subtitle = "Trigger notification check immediately",
          position = ShapePosition.Single,
          onClick = { viewModel.onTriggerNotificationCheck() },
        )
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