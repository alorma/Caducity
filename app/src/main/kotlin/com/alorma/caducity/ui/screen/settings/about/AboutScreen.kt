package com.alorma.caducity.ui.screen.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.previewSettingsModule
import com.alorma.caducity.feature.version.AppVersionProvider
import androidx.compose.ui.res.stringResource
import org.koin.compose.koinInject

@Composable
fun AboutScreen(
  modifier: Modifier = Modifier,
  versionProvider: AppVersionProvider = koinInject()
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp)
      .then(modifier),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    StyledSettingsGroup {
      StyledSettingsCard(
        title = versionProvider.getVersionName(),
        subtitle = "Current version",
        position = ShapePosition.Start,
        onClick = { /* No action for version */ },
      )

      StyledSettingsCard(
        title = stringResource(R.string.about_github_link),
        subtitle = "github.com/alorma/caducity",
        position = ShapePosition.End,
        onClick = {
          // TODO: Open browser to GitHub repository
          // This would require platform-specific implementation
        },
      )
    }
  }
}

@Preview
@Composable
private fun AboutScreenPreview() {
  AppPreview(previewSettingsModule) {
    AboutScreen()
  }
}