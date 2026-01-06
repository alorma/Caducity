package com.alorma.caducity.ui.screen.product.detail

sealed interface ProductDetailState {
  data object Loading : ProductDetailState
  data class Success(
    val product: ProductDetailUiModel,
    val variants: List<ProductDetailVariantUiModel>,
    val standaloneInstances: List<ProductInstanceDetailUiModel>,
  ) : ProductDetailState

  data class Error(val message: String) : ProductDetailState
}
