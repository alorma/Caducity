package com.alorma.caducity.ui.screen.category.detail.product

import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel

sealed interface ProductPageSideEffect {
  // Success events
  data object ItemConsumed : ProductPageSideEffect
  data object ItemFrozen : ProductPageSideEffect
  data object ItemDeleted : ProductPageSideEffect

  // Error events
  data object ConsumeItemFailed : ProductPageSideEffect
  data object FreezeItemFailed : ProductPageSideEffect
  data object DeleteItemFailed : ProductPageSideEffect

  // Bottom sheet events
  data class ShowItemActionsBottomSheet(
    val item: ItemDetailUiModel,
  ) : ProductPageSideEffect

  // Specific validation events
  data class FreezeNotAvailable(val status: ItemStatus) : ProductPageSideEffect
  data class ShowConsumeExpiredWarning(
    val item: ItemDetailUiModel,
  ) : ProductPageSideEffect

  data class ShowConsumeExpiredError(
    val item: ItemDetailUiModel,
    val status: ItemStatus
  ) : ProductPageSideEffect
}
