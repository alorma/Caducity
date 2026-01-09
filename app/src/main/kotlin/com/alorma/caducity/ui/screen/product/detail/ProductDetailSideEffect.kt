package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.domain.model.InstanceStatus

sealed interface ProductDetailSideEffect {
  data class ShowMessage(val message: String) : ProductDetailSideEffect
  data class ShowError(val message: String) : ProductDetailSideEffect
  data class FreezeNotAvailable(val status: InstanceStatus) : ProductDetailSideEffect
  data class ShowConsumeExpiredWarning(
    val instance: ProductInstanceDetailUiModel
  ) : ProductDetailSideEffect
  data class ShowConsumeExpiredError(
    val instance: ProductInstanceDetailUiModel,
    val status: InstanceStatus
  ) : ProductDetailSideEffect
}
