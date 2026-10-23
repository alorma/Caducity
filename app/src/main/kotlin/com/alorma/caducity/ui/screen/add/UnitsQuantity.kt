package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.Remove
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.unit.countPlural
import com.alorma.caducity.ui.unit.icon
import com.alorma.caducity.ui.unit.label

private val StepperHeight = 72.dp
private val StepperMiddleShape = RoundedCornerShape(4.dp)

/**
 * Quantity of a product counted one by one: a big − N + with the name of the subtype next to the number, the subtype
 * chips and the way to the other counting types. While editing a unit there is no quantity, only the type.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UnitsQuantity(
  state: AddState,
  onQuantityChange: (Int) -> Unit,
  onUnitTypeChange: (UnitType) -> Unit,
  onMoreClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    if (!state.isEditing) {
      QuantityStepper(quantity = state.quantity, unitType = state.unitType, onQuantityChange = onQuantityChange)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = stringResource(R.string.add_quantity_how).uppercase(),
        style = CaducityTheme.typography.labelSmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        UnitType.entries.filter { it.parent == UnitParent.UNITS }.forEach { type ->
          UnitTypeChip(type = type, selected = type == state.unitType, onClick = { onUnitTypeChange(type) })
        }
      }
    }
    if (state.resetsQuantity) {
      Text(
        text = stringResource(R.string.unit_type_change_warning),
        style = CaducityTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
      )
    }
    MoreTypesLink(onClick = onMoreClick)
  }
}

@Composable
private fun QuantityStepper(
  quantity: Int,
  unitType: UnitType,
  onQuantityChange: (Int) -> Unit,
) {
  val scheme = CaducityTheme.colorScheme
  val buttonColors =
    ButtonDefaults.buttonColors(
      containerColor = scheme.primaryContainer,
      contentColor = scheme.onPrimaryContainer,
      disabledContainerColor = scheme.surfaceContainerHigh,
      disabledContentColor = scheme.outline,
    )
  Row(
    modifier = Modifier.fillMaxWidth().height(StepperHeight),
    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
  ) {
    Button(
      onClick = { onQuantityChange(quantity - 1) },
      enabled = quantity > 1,
      shape = ButtonGroupDefaults.connectedLeadingButtonShape,
      colors = buttonColors,
      modifier = Modifier.weight(1f).height(StepperHeight),
    ) {
      Icon(Symbols.Outlined.Remove, contentDescription = stringResource(R.string.add_quantity_decrease))
    }
    Surface(
      shape = StepperMiddleShape,
      color = scheme.surfaceContainerHigh,
      modifier = Modifier.weight(2.6f).height(StepperHeight),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Text(
          text = quantity.toString(),
          style = CaducityTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
        )
        Text(
          text = countNoun(unitType, quantity),
          style = CaducityTheme.typography.titleMedium,
          color = scheme.onSurfaceVariant,
          textAlign = TextAlign.Start,
          maxLines = 1,
          modifier = Modifier.padding(start = 8.dp, top = 12.dp),
        )
      }
    }
    Button(
      onClick = { onQuantityChange(quantity + 1) },
      enabled = quantity < AddProductUseCase.MAX_QUANTITY,
      shape = ButtonGroupDefaults.connectedTrailingButtonShape,
      colors = buttonColors,
      modifier = Modifier.weight(1f).height(StepperHeight),
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = stringResource(R.string.add_quantity_increase))
    }
  }
}

/** «tuppers» of «3 tuppers»: the counted name without its number, as the plural of the type spells it. */
@Composable
private fun countNoun(
  unitType: UnitType,
  count: Int,
): String =
  pluralStringResource(unitType.countPlural, count, count)
    .replace(count.toString(), "")
    .trim()

@Composable
private fun UnitTypeChip(
  type: UnitType,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val scheme = CaducityTheme.colorScheme
  FilterChip(
    selected = selected,
    onClick = onClick,
    shape = RoundedCornerShape(26.dp),
    modifier = Modifier.height(52.dp),
    border =
      FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = selected,
        borderColor = scheme.outlineVariant,
        selectedBorderColor = scheme.primary,
        selectedBorderWidth = 2.dp,
      ),
    colors =
      FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        labelColor = scheme.onSurface,
        iconColor = scheme.primary,
        selectedContainerColor = scheme.primaryContainer,
        selectedLabelColor = scheme.onPrimaryContainer,
        selectedLeadingIconColor = scheme.onPrimaryContainer,
      ),
    leadingIcon = {
      Icon(
        imageVector = if (selected) Symbols.Outlined.Check else type.icon,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
    },
    label = { Text(type.label(), style = CaducityTheme.typography.labelLarge, maxLines = 1) },
  )
}

@Composable
private fun MoreTypesLink(onClick: () -> Unit) {
  Row(
    modifier = Modifier.clickable(role = Role.Button, onClick = onClick).padding(vertical = 8.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = stringResource(R.string.add_quantity_more),
      style = CaducityTheme.typography.labelLarge,
      color = CaducityTheme.colorScheme.primary,
    )
    Icon(
      imageVector = Symbols.Outlined.ChevronRight,
      contentDescription = null,
      tint = CaducityTheme.colorScheme.primary,
      modifier = Modifier.size(18.dp),
    )
  }
}
