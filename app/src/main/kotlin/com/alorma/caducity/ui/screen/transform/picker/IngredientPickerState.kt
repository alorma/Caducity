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

/** Status chips of the picker. Expired units are never listed (TODO(transforma-open-2)). */
enum class PickerStatusFilter(
  val status: ExpiryStatus?,
) {
  All(null),
  Soon(ExpiryStatus.SOON),
  Fresh(ExpiryStatus.FRESH),
  Frozen(ExpiryStatus.FROZEN),
}

data class IngredientPickerState(
  val groups: List<ProductGroup> = emptyList(),
  val categories: List<Category> = emptyList(),
  val query: String = "",
  val statusFilter: PickerStatusFilter = PickerStatusFilter.All,
  val categoryFilter: String? = null,
  /** Keys of the selected products; they stay selected while a filter hides them. */
  val selectedKeys: Set<String> = emptySet(),
) {
  val visibleGroups: List<ProductGroup>
    get() = groups.filterPickerGroups(query, statusFilter, categoryFilter)

  val selectedCount: Int get() = selectedKeys.size
}

/** Products with at least one live unit; expired units are left out, and a product with only those disappears. */
fun List<PantryEntry>.toPickerGroups(): List<ProductGroup> =
  filterNot { it.status == ExpiryStatus.EXPIRED }
    .toProductGroups()

fun List<ProductGroup>.filterPickerGroups(
  query: String,
  statusFilter: PickerStatusFilter,
  categoryId: String?,
): List<ProductGroup> {
  val typed = query.trim().normalizeForSearch()
  return filter { group ->
    (categoryId == null || group.categoryId == categoryId) &&
      (typed.isEmpty() || group.name.normalizeForSearch().contains(typed)) &&
      (statusFilter.status == null || group.units.any { it.status == statusFilter.status })
  }
}

/** Keys of the products that own any of [itemIds]. */
fun List<ProductGroup>.keysOf(itemIds: Collection<String>): Set<String> =
  filter { group ->
    group.units.any { it.itemId in itemIds }
  }.map { it.key }.toSet()

/** The closest unit of every selected product: each one enters Transform as an ingredient of one unit. */
fun List<ProductGroup>.nearestItemIds(keys: Set<String>): List<String> =
  filter { it.key in keys }
    .map { it.nearest.itemId }
