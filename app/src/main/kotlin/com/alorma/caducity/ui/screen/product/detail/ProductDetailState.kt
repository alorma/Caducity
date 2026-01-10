package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Stable

sealed interface ProductDetailState {
  data object Loading : ProductDetailState

  @Stable
  data class Success(
    val product: ProductDetailUiModel,
    val todayContent: DateInstancesUiModel?,
    val tomorrowContent: DateInstancesUiModel?,
  ) : ProductDetailState

  @Stable
  data class Error(val message: String) : ProductDetailState
}
