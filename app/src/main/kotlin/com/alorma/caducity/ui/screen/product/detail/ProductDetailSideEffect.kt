package com.alorma.caducity.ui.screen.product.detail

sealed interface ProductDetailSideEffect {
  data class ShowMessage(val message: String) : ProductDetailSideEffect
  data class ShowError(val message: String) : ProductDetailSideEffect
}
