package com.alorma.caducity.ui.screen.category.detail.product

import androidx.compose.runtime.Stable
import com.alorma.caducity.ui.screen.category.detail.DateItemsUiModel
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import kotlinx.collections.immutable.ImmutableList

sealed interface ProductPageState {
  data object Loading : ProductPageState

  @Stable
  data class Success(
    val datedItemsGroups: ImmutableList<DateItemsUiModel>,
    val frozenItems: ImmutableList<ItemDetailUiModel>,
    val consumedItems: ImmutableList<ItemDetailUiModel>,
  ) : ProductPageState

  data class Error(
    val message: String,
  ) : ProductPageState
}
