package com.alorma.caducity.ui.screen.category.detail.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProductPageViewModel(
  private val categoryId: String,
  private val productId: String?,
  getProductItemsUseCase: GetProductItemsUseCase,
  productPageMapper: ProductPageMapper,
) : ViewModel() {

  val state: StateFlow<ProductPageState> = getProductItemsUseCase
    .obtain(categoryId, productId)
    .map<_, ProductPageState> { productItems ->
      productPageMapper.mapToUiModel(productItems)
    }
    .catch { error ->
      emit(ProductPageState.Error(error.message ?: "Unknown error"))
    }
    .stateIn(
      viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = ProductPageState.Loading
    )
}
