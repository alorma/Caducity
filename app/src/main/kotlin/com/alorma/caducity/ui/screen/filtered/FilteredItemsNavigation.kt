package com.alorma.caducity.ui.screen.filtered

sealed interface FilteredItemsNavigation {
  data class Category(
    val categoryId: String,
    val source: String,
  ) : FilteredItemsNavigation
}
