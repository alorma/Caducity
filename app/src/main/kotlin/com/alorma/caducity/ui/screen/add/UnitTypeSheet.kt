package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.alorma.caducity.ui.unit.example
import com.alorma.caducity.ui.unit.icon
import com.alorma.caducity.ui.unit.label

/** Types that can be picked today: the parent tabs of the design arrive with Mesurable and Grapats. */
private val PICKABLE_TYPES = UnitType.entries.filter { it.parent == UnitParent.UNITS }

/** «Com ho comptes?» sheet: the subtypes of Unitats, each with an example of how it reads. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnitTypeSheet(
  selected: UnitType,
  onSelect: (UnitType) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = ScreenGutter).padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
  ) {
    Text(
      text = stringResource(R.string.unit_type_title),
      style = CaducityTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
      modifier = Modifier.padding(bottom = 8.dp),
    )
    Text(
      text = stringResource(R.string.unit_type_section).uppercase(),
      style = CaducityTheme.typography.labelLarge,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp),
    )
    PICKABLE_TYPES.forEachIndexed { index, type ->
      val isSelected = type == selected
      SegmentedListItem(
        checked = isSelected,
        onCheckedChange = { onSelect(type) },
        shapes = ListItemDefaults.segmentedShapes(index, PICKABLE_TYPES.size),
        colors =
          ListItemDefaults.segmentedColors(
            containerColor = CaducityTheme.colorScheme.surfaceContainer,
            contentColor = CaducityTheme.colorScheme.onSurface,
            selectedContainerColor = CaducityTheme.colorScheme.primaryContainer,
            selectedContentColor = CaducityTheme.colorScheme.onPrimaryContainer,
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

@PreviewDynamicLightDark
@Composable
internal fun UnitTypeSheetPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.surfaceContainerLow) {
      UnitTypeSheet(modifier = Modifier.padding(top = 24.dp), selected = UnitType.UNITS, onSelect = {})
    }
  }
}

@PreviewDynamicLightDark
@Composable
internal fun UnitTypeSheetTuppersPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.surfaceContainerLow) {
      UnitTypeSheet(modifier = Modifier.padding(top = 24.dp), selected = UnitType.TUPPERS, onSelect = {})
    }
  }
}
