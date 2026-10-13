package com.alorma.caducity.ui.screen.transform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate

private val today = LocalDate(2026, 10, 12)
private val meat = Category("meat", "Meat", "", CategoryShape.COOKIE, CategoryIcon("set_meal"), 0)
private val produce = Category("produce", "Produce", "", CategoryShape.CLOVER8, CategoryIcon("nutrition"), 1)
private val categories = listOf(meat, produce)

private fun ingredient(
  key: String,
  name: String,
  category: Category,
  units: Int,
  count: Int,
  firstDay: Int,
) = IngredientUi(
  key = key,
  name = name,
  categoryId = category.id,
  units =
    List(units) {
      ActiveItem(
        itemId = "$key-$it",
        productId = key,
        name = name,
        categoryId = category.id,
        expiration = LocalDate(2026, 10, firstDay + it),
        frozen = false,
        frozenSince = null,
        remainingDays = null,
        packSize = null,
        createdAt = null,
      )
    },
  count = count,
)

private val chicken = ingredient("chicken", "Whole chicken", meat, units = 1, count = 1, firstDay = 14)
private val vegetables = ingredient("veg", "Soup vegetables", produce, units = 2, count = 2, firstDay = 16)

private val noActions =
  ResultActions(
    onToggle = {},
    onRemove = {},
    onAdd = {},
    onNameChange = { _, _ -> },
    onSuggestionClick = {},
    onServingsChange = { _, _ -> },
    onCategorySelect = { _, _ -> },
    onPickDate = {},
    onFrozenCountChange = { _, _ -> },
  )

@Composable
private fun TransformPreviewContent(state: TransformState) {
  PreviewTheme {
    Surface {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        IngredientsSection(
          ingredients = state.ingredients,
          categories = state.categories,
          onCountChange = { _, _ -> },
          onRemove = {},
          onAdd = {},
          optional = state.freeEntry,
        )
        ResultsSection(state = state, actions = noActions)
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
private fun TransformEmptyPreview() {
  TransformPreviewContent(
    TransformState(today = today, ingredients = listOf(chicken), categories = categories),
  )
}

@PreviewDynamicLightDark
@Composable
private fun TransformSimplePreview() {
  TransformPreviewContent(
    TransformState(
      today = today,
      ingredients = listOf(chicken, vegetables),
      categories = categories,
      results = listOf(ResultUi(id = 0, name = "Broth", servings = 4, frozenCount = 2)),
    ),
  )
}

@PreviewDynamicLightDark
@Composable
private fun TransformMultiplePreview() {
  TransformPreviewContent(
    TransformState(
      today = today,
      ingredients = listOf(chicken),
      categories = categories,
      results =
        listOf(
          ResultUi(id = 0, name = "Chicken breast", servings = 2, frozenCount = 1),
          ResultUi(id = 1, name = "Carcass broth", servings = 4, categoryOverride = "produce"),
          ResultUi(id = 2),
        ),
      expandedResultId = 1,
    ),
  )
}

@PreviewDynamicLightDark
@Composable
private fun TransformFreeEntryPreview() {
  TransformPreviewContent(TransformState(today = today, categories = categories, freeEntry = true))
}

@PreviewDynamicLightDark
@Composable
private fun TransformFreeEntryFilledPreview() {
  TransformPreviewContent(
    TransformState(
      today = today,
      categories = categories,
      freeEntry = true,
      results =
        listOf(
          ResultUi(
            id = 0,
            name = "Lasagna",
            servings = 4,
            categoryOverride = "meat",
            expirationOverride = LocalDate(2026, 10, 16),
          ),
        ),
    ),
  )
}
