package com.alorma.caducity.ui.screen.settings.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.StyledCenterAlignedTopAppBar
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun DebugSettingsScreen(
  modifier: Modifier = Modifier,
  triggerNotificationCheck: () -> Unit,
) {
  AppScaffold(
    modifier = Modifier.then(modifier),
    containerColor = BottomSheetDefaults.ContainerColor,
    topBar = {
      StyledCenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = BottomSheetDefaults.ContainerColor,
        ),
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
      // Export & Restore Group
      StyledSettingsGroup {
        StyledSettingsCard(
          title = "Test Notification",
          subtitle = "Trigger notification check immediately",
          position = ShapePosition.Single,
          onClick = triggerNotificationCheck,
        )
      }
    }
  }
}


@PreviewDynamicLightDark
@Composable
fun DebugSettingsScreenPreview() {
  PreviewTheme {
    Surface {
      DebugSettingsScreen(
        triggerNotificationCheck = {}
      )
    }
  }
}