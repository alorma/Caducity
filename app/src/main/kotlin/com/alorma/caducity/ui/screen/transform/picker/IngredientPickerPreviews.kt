package com.alorma.caducity.ui.screen.transform.picker

import androidx.compose.runtime.Composable
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate

private val meat = Category("meat", "Meat", "", CategoryShape.COOKIE, CategoryIcon("set_meal"), 0)
private val produce = Category("produce", "Produce", "", CategoryShape.CLOVER8, CategoryIcon("nutrition"), 1)
private val categories = listOf(meat, produce)

private fun entry(
  id: String,
  name: String,
  category: Category,
  status: ExpiryStatus,
  day: Int,
) = PantryEntry(
  itemId = id,
  productId = name,
  name = name,
  categoryId = category.id,
  expiration = LocalDate(2026, 10, day),
  status = status,
  frozenSince = null,
  remainingDays = null,
  packSize = null,
  createdAt = null,
  soonDays = 3,
)

private val groups =
  listOf(
    entry("chicken", "Whole chicken", meat, ExpiryStatus.FROZEN, 30),
    entry("ham", "Sweet ham", meat, ExpiryStatus.SOON, 14),
    entry("carrot-1", "Carrots", produce, ExpiryStatus.FRESH, 20),
    entry("carrot-2", "Carrots", produce, ExpiryStatus.FRESH, 22),
    entry("onion", "Onions", produce, ExpiryStatus.FRESH, 28),
  ).toPickerGroups()

private val noActions =
  IngredientPickerActions(
    onClose = {},
    onConfirm = {},
    onQueryChange = {},
    onStatusFilterToggle = {},
    onCategoryFilterToggle = {},
    onToggle = {},
  )

@Composable
private fun PickerPreviewContent(
  state: IngredientPickerState,
  openMenu: PickerMenu? = null,
) {
  PreviewTheme {
    IngredientPickerContent(state = state, actions = noActions, openMenu = openMenu)
  }
}

@PreviewDynamicLightDark
@Composable
private fun IngredientPickerSelectionPreview() {
  PickerPreviewContent(
    IngredientPickerState(groups = groups, categories = categories, selectedKeys = setOf("Whole chicken", "Carrots")),
  )
}

@PreviewDynamicLightDark
@Composable
private fun IngredientPickerNothingSelectedPreview() {
  PickerPreviewContent(
    IngredientPickerState(groups = groups, categories = categories, categoryFilters = setOf("produce")),
  )
}

@PreviewDynamicLightDark
@Composable
private fun IngredientPickerNoResultsPreview() {
  PickerPreviewContent(IngredientPickerState(groups = groups, categories = categories, query = "zzz"))
}

@PreviewDynamicLightDark
@Composable
private fun IngredientPickerStatusMenuPreview() {
  PickerPreviewContent(
    IngredientPickerState(groups = groups, categories = categories, statusFilters = setOf(ExpiryStatus.SOON)),
    openMenu = PickerMenu.Status,
  )
}

@PreviewDynamicLightDark
@Composable
private fun IngredientPickerCategoryMenuPreview() {
  PickerPreviewContent(
    IngredientPickerState(groups = groups, categories = categories, categoryFilters = setOf("meat", "produce")),
    openMenu = PickerMenu.Category,
  )
}
