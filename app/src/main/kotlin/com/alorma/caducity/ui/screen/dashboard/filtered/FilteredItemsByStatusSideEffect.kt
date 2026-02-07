package com.alorma.caducity.ui.screen.dashboard.filtered

import com.alorma.caducity.domain.model.Item

sealed interface FilteredItemsByStatusSideEffect {
  data class ShowProductItemsBottomSheet(
    val productName: String,
    val items: List<Item>,
  ) : FilteredItemsByStatusSideEffect
}
