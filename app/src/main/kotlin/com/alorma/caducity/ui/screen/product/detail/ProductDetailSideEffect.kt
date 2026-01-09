package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.domain.model.InstanceStatus

sealed interface ProductDetailSideEffect {
  data class ShowMessage(val message: String) : ProductDetailSideEffect
  data class ShowError(val message: String) : ProductDetailSideEffect
  data class FreezeNotAvailable(val status: InstanceStatus) : ProductDetailSideEffect
}
