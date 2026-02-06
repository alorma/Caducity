package com.alorma.caducity.ui.screen.category.detail

sealed interface CategoryDetailSideEffect {
  // Success events
  data object ProductCreated : CategoryDetailSideEffect
  data object ProductDeleted : CategoryDetailSideEffect
  data object CategoryDeleted : CategoryDetailSideEffect

  // Error events
  data object CreateProductFailed : CategoryDetailSideEffect
  data object DeleteProductHasActiveItems : CategoryDetailSideEffect
  data object DeleteProductFailed : CategoryDetailSideEffect
  data object DeleteCategoryFailed : CategoryDetailSideEffect

  // Dialog events
  data object ShowAddProductDialog : CategoryDetailSideEffect
  data class ShowDeleteProductDialog(val productId: String) : CategoryDetailSideEffect
  data object ShowDeleteCategoryDialog : CategoryDetailSideEffect
}
