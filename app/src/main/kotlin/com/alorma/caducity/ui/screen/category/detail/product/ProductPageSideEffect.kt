package com.alorma.caducity.ui.screen.category.detail.product

import com.alorma.caducity.domain.model.ProductDeletionStrategy
import com.alorma.caducity.ui.screen.category.detail.CategoryProductTabUiModel
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel

sealed interface ProductPageSideEffect {
  // Item-level success events
  data object ItemDeleted : ProductPageSideEffect

  // Product-level success events
  data object ProductDeleted : ProductPageSideEffect
  data object ItemsCleared : ProductPageSideEffect

  // Item-level error events
  data object DeleteItemFailed : ProductPageSideEffect

  // Product-level error events
  data object DeleteProductFailed : ProductPageSideEffect
  data object ClearItemsFailed : ProductPageSideEffect

  // Item-level bottom sheet events
  data class ShowItemActionsBottomSheet(
    val item: ItemDetailUiModel,
  ) : ProductPageSideEffect

  // Product-level dialog events
  data class ShowDeleteProductDialog(
    val productId: String,
    val onDeleteProduct: (String, ProductDeletionStrategy) -> Unit,
  ) : ProductPageSideEffect

  data class ShowDeleteProductWithItemsDialog(
    val productId: String,
    val activeItemCount: Int,
    val availableProducts: List<CategoryProductTabUiModel>,
    val onDeleteProduct: (String, ProductDeletionStrategy) -> Unit,
  ) : ProductPageSideEffect

  data class ShowClearProductItemsDialog(
    val productId: String?,
    val onClearProductItems: (String?, Boolean) -> Unit,
  ) : ProductPageSideEffect

  // Navigation events
  data class NavigateToAddItem(
    val categoryId: String,
    val productId: String?
  ) : ProductPageSideEffect
}
