package com.alorma.caducity.ui.screen.settings.about

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
fun AboutScreen(
  appVersion: String,
  onNavigateToRepo: () -> Unit,
  modifier: Modifier = Modifier,
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
            text = stringResource(R.string.settings_about_title),
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
          title = appVersion,
          subtitle = "Current version",
          position = ShapePosition.Start,
          onClick = { /* No action for version */ },
        )

        StyledSettingsCard(
          title = stringResource(R.string.about_github_link),
          subtitle = "github.com/alorma/caducity",
          position = ShapePosition.End,
          onClick = onNavigateToRepo,
        )
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
fun AboutScreenPreview() {
  PreviewTheme {
    Surface {
      AboutScreen(
        appVersion = "1.1.2",
        onNavigateToRepo = {},
      )
    }
  }
}