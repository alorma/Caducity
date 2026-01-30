package com.alorma.caducity.ui.screen.category.detail

import com.alorma.caducity.domain.model.ItemStatus

sealed interface CategoryDetailSideEffect {
  // Success events
  data object ItemConsumed : CategoryDetailSideEffect
  data object ItemFrozen : CategoryDetailSideEffect
  data object ItemDeleted : CategoryDetailSideEffect
  data object ProductCreated : CategoryDetailSideEffect

  // Error events
  data object ConsumeItemFailed : CategoryDetailSideEffect
  data object FreezeItemFailed : CategoryDetailSideEffect
  data object DeleteItemFailed : CategoryDetailSideEffect
  data object CreateProductFailed : CategoryDetailSideEffect

  // Dialog events
  data object ShowAddProductDialog : CategoryDetailSideEffect

  // Bottom sheet events
  data class ShowItemActionsBottomSheet(
    val item: ItemDetailUiModel,
  ) : CategoryDetailSideEffect

  // Specific validation events
  data class FreezeNotAvailable(val status: ItemStatus) : CategoryDetailSideEffect
  data class ShowConsumeExpiredWarning(
    val item: ItemDetailUiModel,
  ) : CategoryDetailSideEffect

  data class ShowConsumeExpiredError(
    val item: ItemDetailUiModel,
    val status: ItemStatus
  ) : CategoryDetailSideEffect
}
