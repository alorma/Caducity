package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.AddedBatch
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate

private val today = LocalDate(2026, 10, 11)
private val dairy = Category("dairy", "Dairy", "", CategoryShape.COOKIE, CategoryIcon("water_full"), 0)
private val produce = Category("produce", "Produce", "", CategoryShape.CLOVER8, CategoryIcon("nutrition"), 1)
private val bakery = Category("bakery", "Bakery", "", CategoryShape.PUFFY, CategoryIcon("bakery_dining"), 2)
private val categories = listOf(dairy, produce, bakery)

private val entry = AddedEntry(AddedBatch("p", listOf("i"), true), "Cheddar", LocalDate(2026, 10, 21), 1)

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
          focusRequester =
            androidx.compose.ui.focus
              .FocusRequester(),
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
        QuantityFrozenRow(quantity = 3, frozen = true, onQuantityChange = {}, onFrozenChange = {})
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
private fun AddBatchPreview() {
  PreviewTheme {
    Surface {
      val state =
        AddState(
          today = today,
          categories = categories,
          categoryId = "dairy",
          date = LocalDate(2026, 10, 21),
          added = listOf(entry),
        )
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BatchEntries(entries = state.added, onEdit = {})
        BatchSummary(
          category = dairy,
          date = state.date,
          today = today,
          onChangeCategory = {},
          onChangeDate = {},
          showCategory = true,
          showDate = true,
        )
        AddBottomBar(state = state, onSaveAndAnother = {}, onSave = {})
      }
    }
  }
}
