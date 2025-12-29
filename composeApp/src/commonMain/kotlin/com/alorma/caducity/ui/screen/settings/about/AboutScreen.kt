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
import caducity.composeapp.generated.resources.about_github_link
import caducity.composeapp.generated.resources.settings_about_title
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.ui.screen.settings.components.CardPosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
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
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

      StyledSettingsCard(
        title = "1.0.0",
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
