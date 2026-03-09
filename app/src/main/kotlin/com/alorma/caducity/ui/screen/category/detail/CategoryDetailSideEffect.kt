package com.alorma.caducity.ui.screen.category.detail

sealed interface CategoryDetailNavigationSideEffect {
  data class NavigateToAddItem(
    val productId: String?,
  ) : CategoryDetailNavigationSideEffect

  data object NavigateBack : CategoryDetailNavigationSideEffect
}

sealed interface CategoryDetailSideEffect {
  // Success events
  data object ProductCreated : CategoryDetailSideEffect

  data object CategoryDeleted : CategoryDetailSideEffect

  // Error events
  data object CreateProductFailed : CategoryDetailSideEffect

  data object DeleteCategoryFailed : CategoryDetailSideEffect

  // Dialog events
  data object ShowAddProductDialog : CategoryDetailSideEffect

  data object ShowDeleteCategoryDialog : CategoryDetailSideEffect
}
