package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.domain.model.InstanceStatus

sealed interface ProductDetailSideEffect {
  // Success events
  data object InstanceConsumed : ProductDetailSideEffect
  data object InstanceFrozen : ProductDetailSideEffect
  data object InstanceDeleted : ProductDetailSideEffect

  // Error events
  data object ConsumeInstanceFailed : ProductDetailSideEffect
  data object FreezeInstanceFailed : ProductDetailSideEffect
  data object DeleteInstanceFailed : ProductDetailSideEffect

  // Specific validation events
  data class FreezeNotAvailable(val status: InstanceStatus) : ProductDetailSideEffect
  data class ShowConsumeExpiredWarning(
    val instance: ProductInstanceDetailUiModel,
  ) : ProductDetailSideEffect

  data class ShowConsumeExpiredError(
    val instance: ProductInstanceDetailUiModel,
    val status: InstanceStatus
  ) : ProductDetailSideEffect
}
