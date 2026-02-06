package com.alorma.caducity.ui.screen.category.detail.product

import androidx.compose.runtime.Stable

sealed interface ProductPageState {
  data object Loading : ProductPageState

  @Stable
  data class Success(
    val uuid: String,
  ) : ProductPageState
}
