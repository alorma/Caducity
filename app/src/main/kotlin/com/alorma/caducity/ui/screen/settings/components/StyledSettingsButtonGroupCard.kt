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
import com.alorma.compose.settings.ui.expressive.SettingsButtonGroup
import com.alorma.compose.settings.ui.expressive.SettingsTileDefaults

@Composable
fun <T> StyledSettingsButtonGroupCard(
  title: String,
  selectedItem: T,
  items: List<T>,
  itemTitleMap: (T) -> String,
  onItemSelected: (T) -> Unit,
  modifier: Modifier = Modifier,
  shapes: ListItemShapes = ListItemDefaults.shapes(),
  subtitle: String? = null,
  icon: (@Composable () -> Unit)? = null,
  colors: ListItemColors = LocalSettingsTileColors.current ?: SettingsTileDefaults.colors(),
) {
  SettingsButtonGroup(
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
    selectedItem = selectedItem,
    items = items,
    itemTitleMap = itemTitleMap,
    onItemSelected = onItemSelected,
  )
}
