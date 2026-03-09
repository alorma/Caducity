package com.alorma.caducity.ui.screen.category.detail

sealed interface CategoryDetailNavigation {
  data class AddItem(
    val productId: String?,
    val source: String,
  ) : CategoryDetailNavigation

  data object CategoryDeleted : CategoryDetailNavigation
}
