package com.alorma.caducity.ui.screen.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.about_developer
import caducity.composeapp.generated.resources.about_github_link
import caducity.composeapp.generated.resources.about_version
import caducity.composeapp.generated.resources.settings_about_title
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutScreen(
  onBack: () -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(Res.string.settings_about_title)) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = AppIcons.Back,
              contentDescription = "Back"
            )
          }
        }
      )
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(paddingValues = paddingValues),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      SettingsGroup(
        title = { Text(text = stringResource(Res.string.about_version)) },
      ) {
        SettingsMenuLink(
          title = { Text(text = "1.0.0") },
          subtitle = { Text(text = "Current version") },
          onClick = { /* No action for version */ },
        )
      }

      SettingsGroup(
        title = { Text(text = stringResource(Res.string.about_developer)) },
      ) {
        SettingsMenuLink(
          title = { Text(text = stringResource(Res.string.about_github_link)) },
          subtitle = { Text(text = "github.com/alorma/caducity") },
          onClick = {
            // TODO: Open browser to GitHub repository
            // This would require platform-specific implementation
          },
        )
      }
    }
  }
}
