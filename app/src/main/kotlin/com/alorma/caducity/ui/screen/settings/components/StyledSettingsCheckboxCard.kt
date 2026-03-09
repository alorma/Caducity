package com.alorma.caducity.ui.screen.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toVerticalShape
import com.alorma.compose.settings.ui.core.LocalSettingsTileColors
import com.alorma.compose.settings.ui.expressive.SettingsCheckbox
import com.alorma.compose.settings.ui.expressive.SettingsTileDefaults

@Composable
fun StyledSettingsCheckboxCard(
  title: String,
  state: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  shapes: ListItemShapes = ListItemDefaults.shapes(),
  subtitle: String? = null,
  icon: (@Composable () -> Unit)? = null,
  colors: ListItemColors = LocalSettingsTileColors.current ?: SettingsTileDefaults.colors(),
) {
  SettingsCheckbox(
    modifier = modifier.fillMaxWidth(),
    shapes = shapes,
    icon = icon,
    colors = colors,
    title = {
      Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
      )
    },
    subtitle = subtitle?.let { { Text(text = it) } },
    state = state,
    onCheckedChange = onCheckedChange,
  )
}
