package com.alorma.caducity.ui.screen.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.alorma.compose.settings.ui.core.LocalSettingsTileColors
import com.alorma.compose.settings.ui.expressive.SettingsMenuLink
import com.alorma.compose.settings.ui.expressive.SettingsTileDefaults

@Composable
fun StyledSettingsCard(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  shapes: ListItemShapes = ListItemDefaults.shapes(),
  icon: (@Composable () -> Unit)? = null,
  action: (@Composable () -> Unit)? = null,
  enabled: Boolean = true,
  colors: ListItemColors = LocalSettingsTileColors.current ?: SettingsTileDefaults.colors(),
) {
  SettingsMenuLink(
    modifier = modifier.fillMaxWidth(),
    shapes = shapes,
    icon = icon,
    title = {
      Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
      )
    },
    subtitle = if (subtitle != null
    ) {
      { Text(text = subtitle) }
    } else {
      null
    },
    action = action,
    onClick = onClick,
    enabled = enabled,
    colors = colors,
  )
}
