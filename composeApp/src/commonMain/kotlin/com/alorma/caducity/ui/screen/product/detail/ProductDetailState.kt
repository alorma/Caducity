package com.alorma.caducity.ui.screen.product.detail

sealed interface ProductDetailState {
  data object Loading : ProductDetailState
  data class Success(
    val product: ProductDetailUiModel,
  ) : ProductDetailState
  data class Error(val message: String) : ProductDetailState
}
