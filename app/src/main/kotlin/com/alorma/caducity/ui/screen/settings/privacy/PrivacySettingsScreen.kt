package com.alorma.caducity.ui.screen.settings.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import org.koin.compose.viewmodel.koinViewModel
import com.alorma.caducity.feature.tracking.PrivacySettingsScreen as PrivacySettingsScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen

@Composable
fun PrivacySettingsScreen(
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PrivacySettingsViewModel = koinViewModel(),
) {
  TrackScreen(screen = PrivacySettingsScreenEvent())

  val state by viewModel.state.collectAsStateWithLifecycle()

  PrivacySettingsContent(
    modifier = modifier,
    analyticsEnabled = state.analyticsEnabled,
    onAnalyticsToggle = viewModel::onAnalyticsToggle,
    onClose = onClose,
  )
}

@Composable
fun PrivacySettingsContent(
  analyticsEnabled: Boolean,
  onAnalyticsToggle: (Boolean) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = Modifier.then(modifier),
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = {
          Text(
            text = stringResource(R.string.settings_privacy_screen_title),
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
        item {
          StyledSettingsGroup(
            title = { Text(stringResource(R.string.settings_privacy_title)) }
          ) {
            StyledSettingsSwitchCard(
              title = stringResource(R.string.settings_privacy_analytics_title),
              subtitle = stringResource(R.string.settings_privacy_analytics_description),
              state = analyticsEnabled,
              position = ShapePosition.Single,
              onCheckedChange = onAnalyticsToggle,
            )
          }
        }

        item {
          Text(
            text = stringResource(R.string.settings_privacy_info),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
      }
    }
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
fun PrivacySettingsScreenPreview() {
  PreviewTheme {
    Surface {
      PrivacySettingsContent(
        analyticsEnabled = true,
        onAnalyticsToggle = {},
        onClose = {},
      )
    }
  }
}
