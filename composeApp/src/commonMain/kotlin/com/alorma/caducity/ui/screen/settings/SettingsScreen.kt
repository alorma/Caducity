package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.settings_about_description
import caducity.composeapp.generated.resources.settings_about_title
import caducity.composeapp.generated.resources.settings_appearance_description
import caducity.composeapp.generated.resources.settings_appearance_title
import caducity.composeapp.generated.resources.settings_notifications_description
import caducity.composeapp.generated.resources.settings_notifications_title
import caducity.composeapp.generated.resources.settings_screen_title
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Palette
import com.alorma.caducity.ui.screen.settings.components.CardPosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
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
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      // Centered Title
      Text(
        text = stringResource(Res.string.settings_screen_title),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 36.dp)
      )

      // Group 1: Appearance & Notifications
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Palette,
              contentDescription = null,
            )
          },
          title = stringResource(Res.string.settings_appearance_title),
          subtitle = stringResource(Res.string.settings_appearance_description),
          onClick = onNavigateToAppearance,
          position = CardPosition.Top,
        )

        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Notifications,
              contentDescription = null,
            )
          },
          title = stringResource(Res.string.settings_notifications_title),
          subtitle = stringResource(Res.string.settings_notifications_description),
          onClick = onNavigateToNotifications,
          position = CardPosition.Bottom,
        )
      }

      // Group 2: About
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Info,
              contentDescription = null,
            )
          },
          title = stringResource(Res.string.settings_about_title),
          subtitle = stringResource(Res.string.settings_about_description),
          onClick = onNavigateToAbout,
          position = CardPosition.Single,
        )
      }
    }
  }
}

