package com.alorma.caducity.ui.screen.category.detail

sealed interface CategoryDetailSideEffect {
  // Navigation side effects
  data class NavigateToAddItem(val productId: String?) : CategoryDetailSideEffect
  data object NavigateBack : CategoryDetailSideEffect

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
