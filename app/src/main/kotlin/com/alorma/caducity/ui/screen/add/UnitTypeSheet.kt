package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toHorizontalShape
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.alorma.caducity.ui.unit.example
import com.alorma.caducity.ui.unit.icon
import com.alorma.caducity.ui.unit.label
import com.alorma.caducity.ui.unit.rule

/** Parents that can be picked today: Grapats arrives with its own phase. */
private val PICKABLE_PARENTS = listOf(UnitParent.UNITS, UnitParent.MEASURED)

/** «Com ho comptes?» sheet: first the parent type (tabs), then the name inside it, each with an example of how it reads. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnitTypeSheet(
  selected: UnitType,
  onSelect: (UnitType) -> Unit,
  modifier: Modifier = Modifier,
) {
  var parent by remember { mutableStateOf(selected.parent) }
  val types = UnitType.entries.filter { it.parent == parent }
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = ScreenGutter).padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
  ) {
    Text(
      text = stringResource(R.string.unit_type_title),
      style = CaducityTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    )
    Text(
      text = stringResource(R.string.unit_type_subtitle),
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 12.dp),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
      PICKABLE_PARENTS.forEachIndexed { index, option ->
        ParentTab(
          parent = option,
          selected = option == parent,
          position = tabPosition(index, PICKABLE_PARENTS.size),
          onClick = { parent = option },
          modifier = Modifier.weight(1f),
        )
      }
    }
    Text(
      text = parent.rule(),
      style = CaducityTheme.typography.bodySmall,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    )
    Text(
      text = stringResource(R.string.unit_type_section).uppercase(),
      style = CaducityTheme.typography.labelLarge,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
    )
    types.forEachIndexed { index, type ->
      val isSelected = type == selected
      SegmentedListItem(
        checked = isSelected,
        onCheckedChange = { onSelect(type) },
        shapes = ListItemDefaults.segmentedShapes(index, types.size),
        colors =
          ListItemDefaults.segmentedColors(
            containerColor = CaducityTheme.colorScheme.surfaceContainer,
            contentColor = CaducityTheme.colorScheme.onSurface,
            selectedContainerColor = CaducityTheme.colorScheme.primaryContainer,
            selectedContentColor = CaducityTheme.colorScheme.onPrimaryContainer,
            supportingContentColor = CaducityTheme.colorScheme.onSurfaceVariant,
            selectedSupportingContentColor = CaducityTheme.colorScheme.onPrimaryContainer,
          ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        leadingContent = {
          Icon(
            imageVector = type.icon,
            contentDescription = null,
            tint = CaducityTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        },
        supportingContent = { Text(type.example()) },
        trailingContent = {
          if (isSelected) {
            Icon(
              imageVector = Symbols.Outlined.Check,
              contentDescription = null,
              tint = CaducityTheme.colorScheme.primary,
            )
          }
        },
      ) {
        Text(type.label(), style = CaducityTheme.typography.titleMedium)
      }
    }
  }
}

private fun tabPosition(
  index: Int,
  count: Int,
): ShapePosition =
  when {
    count == 1 -> ShapePosition.Single
    index == 0 -> ShapePosition.Start
    index == count - 1 -> ShapePosition.End
    else -> ShapePosition.Middle
  }

@Composable
private fun ParentTab(
  parent: UnitParent,
  selected: Boolean,
  position: ShapePosition,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scheme = CaducityTheme.colorScheme
  Surface(
    shape = position.toHorizontalShape(),
    color = if (selected) scheme.primary else scheme.surfaceContainer,
    contentColor = if (selected) scheme.onPrimary else scheme.onSurface,
    modifier = modifier.heightIn(min = 72.dp).selectable(selected = selected, role = Role.Tab, onClick = onClick),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
      Icon(imageVector = parent.icon, contentDescription = null, modifier = Modifier.size(24.dp))
      Text(parent.label(), style = CaducityTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }
  }
}

@PreviewDynamicLightDark
@Composable
internal fun UnitTypeSheetPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.surfaceContainerLow) {
      UnitTypeSheet(selected = UnitType.UNITS, onSelect = {}, modifier = Modifier.padding(top = 24.dp))
    }
  }
}

@PreviewDynamicLightDark
@Composable
internal fun UnitTypeSheetTuppersPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.surfaceContainerLow) {
      UnitTypeSheet(selected = UnitType.TUPPERS, onSelect = {}, modifier = Modifier.padding(top = 24.dp))
    }
  }
}

@PreviewDynamicLightDark
@Composable
internal fun UnitTypeSheetMeasurablePreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.surfaceContainerLow) {
      UnitTypeSheet(selected = UnitType.GRAMS, onSelect = {}, modifier = Modifier.padding(top = 24.dp))
    }
  }
}
