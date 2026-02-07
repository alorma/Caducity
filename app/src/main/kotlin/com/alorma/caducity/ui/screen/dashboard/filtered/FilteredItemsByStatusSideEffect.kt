package com.alorma.caducity.ui.screen.dashboard.filtered

import com.alorma.caducity.domain.model.Item

sealed interface FilteredItemsByStatusSideEffect {
  data class ShowProductItemsBottomSheet(
    val productName: String,
    val items: List<Item>,
  ) : FilteredItemsByStatusSideEffect

  data class ShowItemActionsBottomSheet(
    val item: Item,
  ) : FilteredItemsByStatusSideEffect

  data object ItemConsumed : FilteredItemsByStatusSideEffect
  data object ItemFrozen : FilteredItemsByStatusSideEffect
  data object ItemUnfrozen : FilteredItemsByStatusSideEffect
  data object ItemDeleted : FilteredItemsByStatusSideEffect

  data class ItemActionFailed(val message: String) : FilteredItemsByStatusSideEffect
}
