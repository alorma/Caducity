package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.settings_about_description
import caducity.composeapp.generated.resources.settings_about_title
import caducity.composeapp.generated.resources.settings_appearance_description
import caducity.composeapp.generated.resources.settings_appearance_title
import caducity.composeapp.generated.resources.settings_notifications_description
import caducity.composeapp.generated.resources.settings_notifications_title
import caducity.composeapp.generated.resources.settings_screen_title
import com.alorma.compose.settings.ui.SettingsMenuLink
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsRootScreen(
  onNavigateToAppearance: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onNavigateToAbout: () -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(Res.string.settings_screen_title)) },
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
      SettingsMenuLink(
        title = { Text(text = stringResource(Res.string.settings_appearance_title)) },
        subtitle = { Text(text = stringResource(Res.string.settings_appearance_description)) },
        onClick = onNavigateToAppearance,
      )
      SettingsMenuLink(
        title = { Text(text = stringResource(Res.string.settings_notifications_title)) },
        subtitle = { Text(text = stringResource(Res.string.settings_notifications_description)) },
        onClick = onNavigateToNotifications,
      )
      SettingsMenuLink(
        title = { Text(text = stringResource(Res.string.settings_about_title)) },
        subtitle = { Text(text = stringResource(Res.string.settings_about_description)) },
        onClick = onNavigateToAbout,
      )
    }
  }
}
