package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toVerticalShape
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.unit.icon
import com.alorma.caducity.ui.unit.label

/** «Com ho comptes?» row: the current type on the right; it opens the type sheet. */
@Composable
fun UnitTypeRow(
  unitType: UnitType,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  /** Position in the connected group with the quantity row below it. */
  shapePosition: ShapePosition = ShapePosition.Start,
) {
  Surface(
    shape = shapePosition.toVerticalShape(),
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    modifier = modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
  ) {
    Row(
      modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Icon(
        imageVector = unitType.icon,
        contentDescription = null,
        tint = CaducityTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp),
      )
      Text(
        text = stringResource(R.string.unit_type_title),
        style = CaducityTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.weight(1f),
      )
      Text(
        text = unitType.label(),
        style = CaducityTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        color = CaducityTheme.colorScheme.primary,
      )
      Icon(
        imageVector = Symbols.Outlined.ChevronRight,
        contentDescription = null,
        tint = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}
