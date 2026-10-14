package com.alorma.caducity.ui.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.compose.settings.ui.core.LocalSettingsTileColors
import com.alorma.compose.settings.ui.expressive.SettingsTileDefaults
import com.alorma.compose.settings.ui.expressive.SettingsTileScaffold

/**
 * A settings row with a connected group of a few options under its title («Aviat», «Tema»): native M3E
 * [ToggleButton]s, 40 dp high, the selected one with a ✓.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
  SettingsTileScaffold(
    modifier = modifier.fillMaxWidth(),
    shapes = shapes,
    leadingContent = icon,
    colors = colors,
    title = { Text(text = title, fontWeight = FontWeight.SemiBold) },
    supportingContent = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        subtitle?.let { Text(text = it) }
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp).selectableGroup(),
          horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
          items.forEachIndexed { index, item ->
            val checked = item == selectedItem
            ToggleButton(
              checked = checked,
              onCheckedChange = { onItemSelected(item) },
              modifier = Modifier.weight(1f).heightIn(min = 40.dp).semantics { role = Role.RadioButton },
              shapes =
                when (index) {
                  0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                  items.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                  else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
              colors = ToggleButtonDefaults.colors(containerColor = CaducityTheme.colorScheme.surfaceContainerHigh),
            ) {
              if (checked) {
                Icon(Symbols.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
              }
              Text(text = itemTitleMap(item), maxLines = 2, textAlign = TextAlign.Center)
            }
          }
        }
      }
    },
  )
}
