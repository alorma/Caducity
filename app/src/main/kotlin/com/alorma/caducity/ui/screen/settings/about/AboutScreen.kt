package com.alorma.caducity.ui.screen.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.alorma.caducity.feature.tracking.AboutScreen as AboutScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen

@Composable
fun AboutScreen(
  appVersion: String,
  onNavigateToRepo: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TrackScreen(screen = AboutScreenEvent())
  AboutScreenContent(
    appVersion = appVersion,
    onNavigateToRepo = onNavigateToRepo,
    modifier = modifier,
  )
}

@Composable
private fun AboutScreenContent(
  appVersion: String,
  onNavigateToRepo: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = Modifier.then(modifier),
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = {
          Text(
            text = stringResource(R.string.settings_about_title),
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
      // Export & Restore Group
      item {
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
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
fun AboutScreenPreview() {
  PreviewTheme {
    Surface {
      AboutScreenContent(
        appVersion = "1.1.2",
        onNavigateToRepo = {},
      )
    }
  }
}
