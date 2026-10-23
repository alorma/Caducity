package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.HourglassBottom
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.threshold.thresholdLabel
import com.alorma.caducity.ui.theme.CaducityTheme

/** Title of a sheet of the Add screen. */
@Composable
private fun SheetTitle(
  title: String,
  subtitle: String? = null,
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = title,
      style = CaducityTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    )
    if (subtitle != null) {
      Text(
        text = subtitle,
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun SheetColumn(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    content()
  }
}

/** «Categoria» sheet: tapping a chip picks it and closes the sheet. */
@Composable
internal fun CategorySheet(
  state: AddState,
  onSelect: (String) -> Unit,
  onNew: () -> Unit,
) {
  SheetColumn {
    SheetTitle(
      title = stringResource(R.string.add_category_label),
      subtitle = stringResource(R.string.add_category_sheet_subtitle),
    )
    CategoryChips(
      categories = state.categories,
      selectedId = state.categoryId,
      highlightNew = state.isNewProduct && state.categoryId == null,
      onSelect = onSelect,
      onNew = onNew,
    )
  }
}

/** «Caduca» sheet: typed date, shortcuts and the category's usual duration. */
@Composable
internal fun DateSheet(
  state: AddState,
  onDigitsChange: (String) -> Unit,
  onPickerClick: () -> Unit,
  onQuickDate: (QuickDate) -> Unit,
  onChangeSuggestedDuration: () -> Unit,
  onDone: () -> Unit,
) {
  SheetColumn {
    SheetTitle(
      title = stringResource(R.string.add_date_label),
      subtitle = stringResource(R.string.add_date_sheet_subtitle),
    )
    ExpiryDateField(
      digits = state.dateDigits,
      date = state.date,
      today = state.today,
      onDigitsChange = onDigitsChange,
      onPickerClick = onPickerClick,
    )
    QuickDateGrid(onSelect = onQuickDate, date = state.date, today = state.today)
    val category = state.category
    if (category != null) {
      val usualDays = category.usualDurationDays
      Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onChangeSuggestedDuration),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
          imageVector = Symbols.Outlined.HourglassBottom,
          contentDescription = null,
          tint = CaducityTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        Text(
          text =
            if (usualDays != null) {
              stringResource(R.string.add_date_suggested, category.name, thresholdLabel(usualDays))
            } else {
              stringResource(R.string.add_date_no_usual, category.name)
            },
          style = CaducityTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f),
        )
        Text(
          text =
            stringResource(
              if (usualDays != null) R.string.add_date_suggested_change else R.string.add_date_set_one,
            ),
          style = CaducityTheme.typography.labelLarge,
          color = CaducityTheme.colorScheme.primary,
          modifier = Modifier.padding(vertical = 8.dp),
        )
      }
    }
    DoneButton(onDone)
  }
}

/** «Quantitat» sheet: how it is counted and how many. Editing a unit only changes the type of the product. */
@Composable
internal fun QuantitySheet(
  state: AddState,
  onUnitTypeClick: () -> Unit,
  onUnitTypeChange: (UnitType) -> Unit,
  onQuantityChange: (Int) -> Unit,
  onAmountChange: (Int) -> Unit,
  onLevelChange: (HandfulLevel) -> Unit,
  onDone: () -> Unit,
) {
  SheetColumn {
    SheetTitle(title = stringResource(R.string.add_quantity))
    if (state.unitType.parent == UnitParent.UNITS) {
      UnitsQuantity(
        state = state,
        onQuantityChange = onQuantityChange,
        onUnitTypeChange = onUnitTypeChange,
        onMoreClick = onUnitTypeClick,
      )
    } else {
      MeasuredOrHandfulQuantity(state, onUnitTypeClick, onQuantityChange, onAmountChange, onLevelChange)
    }
    DoneButton(onDone)
  }
}

/** Measured (g, mL) and handful products keep their own controls under the row that changes the type. */
@Composable
private fun MeasuredOrHandfulQuantity(
  state: AddState,
  onUnitTypeClick: () -> Unit,
  onQuantityChange: (Int) -> Unit,
  onAmountChange: (Int) -> Unit,
  onLevelChange: (HandfulLevel) -> Unit,
) {
  Column {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        UnitTypeRow(
          unitType = state.unitType,
          onClick = onUnitTypeClick,
          shapePosition = if (state.isEditing) ShapePosition.Single else ShapePosition.Start,
        )
        if (state.resetsQuantity) {
          Text(
            text = stringResource(R.string.unit_type_change_warning),
            style = CaducityTheme.typography.bodySmall,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
        if (!state.isEditing && state.isHandful) {
          LevelFrozenRow(
            level = state.level,
            frozen = false,
            onLevelChange = onLevelChange,
            onFrozenChange = {},
            canFreeze = false,
          )
        } else if (!state.isEditing) {
          QuantityFrozenRow(
            quantity = state.quantity,
            unitType = state.unitType,
            amount = state.amount,
            onAmountChange = onAmountChange,
            frozen = false,
            onQuantityChange = onQuantityChange,
            onFrozenChange = {},
            canFreeze = false,
          )
        }
      }
      if (!state.isEditing && state.isMeasured) {
        AmountChips(unitType = state.unitType, amount = state.amount, onSelect = onAmountChange)
      }
    }
  }
}

@Composable
private fun DoneButton(onDone: () -> Unit) {
  Button(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
    Icon(Symbols.Outlined.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
    Text(stringResource(R.string.category_editor_done))
  }
}
