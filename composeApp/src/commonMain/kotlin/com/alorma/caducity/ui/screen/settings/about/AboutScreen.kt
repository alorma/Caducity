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
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.about_github_link
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.settings.components.CardPosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.previewSettingsModule
import com.alorma.caducity.version.AppVersionProvider
import org.jetbrains.compose.resources.stringResource
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
        position = CardPosition.Top,
        onClick = { /* No action for version */ },
      )

      StyledSettingsCard(
        title = stringResource(Res.string.about_github_link),
        subtitle = "github.com/alorma/caducity",
        position = CardPosition.Bottom,
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