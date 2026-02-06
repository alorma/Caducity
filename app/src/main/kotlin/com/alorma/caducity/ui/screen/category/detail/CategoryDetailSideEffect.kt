package com.alorma.caducity.ui.screen.category.detail

import com.alorma.caducity.domain.model.ProductDeletionStrategy

sealed interface CategoryDetailSideEffect {
  // Success events
  data object ProductCreated : CategoryDetailSideEffect
  data object ProductDeleted : CategoryDetailSideEffect
  data object CategoryDeleted : CategoryDetailSideEffect

  // Error events
  data object CreateProductFailed : CategoryDetailSideEffect
  data object DeleteProductFailed : CategoryDetailSideEffect
  data object DeleteCategoryFailed : CategoryDetailSideEffect

  // Dialog events
  data object ShowAddProductDialog : CategoryDetailSideEffect

  data class ShowClearProductItemsDialog(
    val productId: String,
  ) : CategoryDetailSideEffect

  data class ShowDeleteProductDialog(
    val productId: String,
    val onDeleteProduct: (String, ProductDeletionStrategy) -> Unit,
  ) : CategoryDetailSideEffect

  data class ShowDeleteProductWithItemsDialog(
    val productId: String,
    val activeItemCount: Int,
    val availableProducts: List<CategoryProductTabUiModel>,
    val onDeleteProduct: (String, ProductDeletionStrategy) -> Unit,
  ) : CategoryDetailSideEffect
  data object ShowDeleteCategoryDialog : CategoryDetailSideEffect
}
