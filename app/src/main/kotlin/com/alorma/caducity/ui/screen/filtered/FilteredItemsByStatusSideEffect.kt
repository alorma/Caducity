package com.alorma.caducity.ui.screen.filtered

import com.alorma.caducity.domain.model.Item

sealed interface FilteredItemsByStatusNavigationSideEffect {
  data class NavigateToCategory(val categoryId: String) : FilteredItemsByStatusNavigationSideEffect
}

sealed interface FilteredItemsByStatusSideEffect {
  // Bottom sheet side effects
  data class ShowProductItemsBottomSheet(
    val productName: String,
    val items: List<Item>,
  ) : FilteredItemsByStatusSideEffect

  data class ShowItemActionsBottomSheet(
    val item: Item,
  ) : FilteredItemsByStatusSideEffect

  // Success side effects
  data object ItemConsumed : FilteredItemsByStatusSideEffect
  data object ItemFrozen : FilteredItemsByStatusSideEffect
  data object ItemUnfrozen : FilteredItemsByStatusSideEffect
  data object ItemDeleted : FilteredItemsByStatusSideEffect

  // Error side effects
  data class ItemActionFailed(val message: String) : FilteredItemsByStatusSideEffect
}
