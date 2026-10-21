package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate

private val today = LocalDate(2026, 10, 11)
private val dairy = Category("dairy", "Dairy", "", CategoryShape.COOKIE, CategoryIcon("water_full"), 0)
private val produce = Category("produce", "Produce", "", CategoryShape.COOKIE7, CategoryIcon("nutrition"), 1)
private val bakery = Category("bakery", "Bakery", "", CategoryShape.PUFFY, CategoryIcon("bakery_dining"), 2)
private val categories = listOf(dairy, produce, bakery)

@PreviewDynamicLightDark
@Composable
private fun AddFormPreview() {
  PreviewTheme {
    Surface {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NameField(
          name = "Whole milk",
          onNameChange = {
          },
          onClear = {},
          focusRequester = remember { FocusRequester() },
        )
        SuggestionRow(suggestion = "Whole milk", typed = "whole", categoryName = "Dairy", onClick = {})
        NewProductNotice()
        CategoryChips(
          categories = categories,
          selectedId = "dairy",
          highlightNew = false,
          onSelect = {},
          onNew = {},
        )
        ExpiryDateField("211026", LocalDate(2026, 10, 21), today, {}, {})
        QuickDateRow(onSelect = {})
        UnitTypeRow(unitType = UnitType.UNITS, onClick = {})
        QuantityFrozenRow(quantity = 3, frozen = true, onQuantityChange = {}, onFrozenChange = {})
        UnitTypeRow(unitType = UnitType.GRAMS, onClick = {})
        QuantityFrozenRow(
          quantity = 1,
          unitType = UnitType.GRAMS,
          amount = 500,
          frozen = false,
          onQuantityChange = {},
          onFrozenChange = {},
        )
        AmountChips(unitType = UnitType.GRAMS, amount = 500, onSelect = {})
        UnitTypeRow(unitType = UnitType.HANDFULS, onClick = {})
        LevelFrozenRow(level = HandfulLevel.HALF, frozen = false, onLevelChange = {}, onFrozenChange = {})
        CanFreezeCard(
          canFreeze = true,
          categoryName = "Dairy",
          categoryCanFreeze = true,
          onCanFreezeChange = {},
        )
        QuantityFrozenRow(
          quantity = 1,
          frozen = false,
          onQuantityChange = {},
          onFrozenChange = {},
          canFreeze = false,
        )
        CanFreezeCard(
          canFreeze = false,
          categoryName = "Pantry",
          categoryCanFreeze = false,
          onCanFreezeChange = {},
        )
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
private fun AddBottomBarPreview() {
  PreviewTheme {
    Surface {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AddBottomBar(
          state =
            AddState(
              today = today,
              name = "Whole milk",
              categories = categories,
              categoryId = "dairy",
              date = LocalDate(2026, 10, 21),
            ),
          onSave = {},
        )
        AddBottomBar(state = AddState(today = today, name = "Whole milk"), onSave = {})
      }
    }
  }
}
