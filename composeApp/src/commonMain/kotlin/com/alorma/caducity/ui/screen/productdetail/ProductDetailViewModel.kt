package com.alorma.caducity.ui.screen.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainProductDetailUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProductDetailViewModel(
  productId: String,
  obtainProductDetailUseCase: ObtainProductDetailUseCase,
  productDetailMapper: ProductDetailMapper,
) : ViewModel() {

  val state: StateFlow<ProductDetailState> = obtainProductDetailUseCase
    .obtainProductDetail(productId)
    .map { result ->
      result.fold(
        onSuccess = { productWithInstances ->
          val productDetail = productDetailMapper.mapToProductDetail(productWithInstances)
          ProductDetailState.Success(productDetail)
        },
        onFailure = { error ->
          ProductDetailState.Error(error.message ?: "Product not found")
        }
      )
    }
    .catch { error ->
      emit(ProductDetailState.Error(error.message ?: "Unknown error"))
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      ProductDetailState.Loading
    )
}
