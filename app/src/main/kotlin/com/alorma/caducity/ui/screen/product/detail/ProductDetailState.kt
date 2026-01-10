package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

sealed interface ProductDetailState {
  data object Loading : ProductDetailState

  @Stable
  data class Success(
    val product: ProductDetailUiModel,
    val content: ImmutableList<DateInstancesUiModel>,
  ) : ProductDetailState

  @Stable
  data class Error(val message: String) : ProductDetailState
}
