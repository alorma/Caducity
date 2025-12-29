package com.alorma.caducity.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
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
import com.alorma.caducity.base.ui.theme.CaducityTheme
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
          shape = RoundedCornerShape(
            topStart = CaducityTheme.shapes.extraLarge.topStart,
            topEnd = CaducityTheme.shapes.extraLarge.topEnd,
            bottomStart = CaducityTheme.shapes.small.bottomStart,
            bottomEnd = CaducityTheme.shapes.small.bottomEnd,
          ),
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
          shape = RoundedCornerShape(
            topStart = CaducityTheme.shapes.small.topStart,
            topEnd = CaducityTheme.shapes.small.topEnd,
            bottomStart = CaducityTheme.shapes.extraLarge.bottomStart,
            bottomEnd = CaducityTheme.shapes.extraLarge.bottomEnd,
          ),
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
          shape = CaducityTheme.shapes.extraLarge,
        )
      }
    }
  }
}

@Composable
private fun StyledSettingsCard(
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  shape: Shape,
  modifier: Modifier = Modifier,
  icon: @Composable () -> Unit
) {
  SettingsMenuLink(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape),
    icon = icon,
    title = {
      Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
      )
    },
    subtitle = { Text(text = subtitle) },
    onClick = onClick,
  )
}
