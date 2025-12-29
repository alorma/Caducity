package com.alorma.caducity.ui.screen.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.alorma.compose.settings.ui.expressive.SettingsButtonGroup

@Composable
fun <T> StyledSettingsButtonGroupCard(
  title: String,
  selectedItem: T,
  items: List<T>,
  itemTitleMap: (T) -> String,
  onItemSelected: (T) -> Unit,
  modifier: Modifier = Modifier,
  position: CardPosition = CardPosition.Single,
  subtitle: String? = null,
  icon: (@Composable () -> Unit)? = null
) {
  SettingsButtonGroup(
    modifier = modifier
      .fillMaxWidth()
      .clip(position.toShape()),
    icon = icon,
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
