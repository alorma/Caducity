package com.alorma.caducity.ui.screen.transform.picker

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.ProductGroup
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.model.toProductGroups

/** Arguments of the picker route: units that are already ingredients, so their products start selected. */
data class IngredientPickerArgs(
  val selectedItemIds: List<String> = emptyList(),
)

/** Statuses the picker can filter by. Expired units are never listed (TODO(transforma-open-2)). */
val PickerStatuses = listOf(ExpiryStatus.SOON, ExpiryStatus.FRESH, ExpiryStatus.FROZEN)

data class IngredientPickerState(
  val groups: List<ProductGroup> = emptyList(),
  val categories: List<Category> = emptyList(),
  val query: String = "",
  /** Empty means no filter. */
  val statusFilters: Set<ExpiryStatus> = emptySet(),
  val categoryFilters: Set<String> = emptySet(),
  /** Keys of the selected products; they stay selected while a filter hides them. */
  val selectedKeys: Set<String> = emptySet(),
) {
  val visibleGroups: List<ProductGroup>
    get() = groups.filterPickerGroups(query, statusFilters, categoryFilters)

  val selectedCount: Int get() = selectedKeys.size

  /** Statuses present among the listed products, in display order. */
  val availableStatuses: List<ExpiryStatus>
    get() = PickerStatuses.filter { status -> groups.any { group -> group.units.any { it.status == status } } }

  /** Categories with listed products. */
  val availableCategories: List<Category>
    get() = categories.filter { category -> groups.any { it.categoryId == category.id } }

  val showStatusFilter: Boolean get() = availableStatuses.size > 1

  val showCategoryFilter: Boolean get() = availableCategories.size > 1
}

/** Products with at least one live unit; expired units are left out, and a product with only those disappears. */
fun List<PantryEntry>.toPickerGroups(): List<ProductGroup> =
  filterNot { it.status == ExpiryStatus.EXPIRED }
    .toProductGroups()

fun List<ProductGroup>.filterPickerGroups(
  query: String,
  statusFilters: Set<ExpiryStatus>,
  categoryIds: Set<String>,
): List<ProductGroup> {
  val typed = query.trim().normalizeForSearch()
  return filter { group ->
    (categoryIds.isEmpty() || group.categoryId in categoryIds) &&
      (typed.isEmpty() || group.name.normalizeForSearch().contains(typed)) &&
      (statusFilters.isEmpty() || group.units.any { it.status in statusFilters })
  }
}

/** Keys of the products that own any of [itemIds]. */
fun List<ProductGroup>.keysOf(itemIds: Collection<String>): Set<String> =
  filter { group ->
    group.units.any { it.itemId in itemIds }
  }.map { it.key }.toSet()

/**
 * One unit per selected product, which enters Transform as an ingredient: the unit that was already an
 * ingredient ([current]) or, for a new one, the closest to expire.
 */
fun List<ProductGroup>.chosenItemIds(
  keys: Set<String>,
  current: Collection<String>,
): List<String> =
  filter { it.key in keys }
    .map { group -> group.units.firstOrNull { it.itemId in current }?.itemId ?: group.nearest.itemId }
