package com.alorma.caducity.ui.screen.product.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.usecase.ObtainProductDetailUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductDetailViewModel(
  productId: String,
  obtainProductDetailUseCase: ObtainProductDetailUseCase,
  productDetailMapper: ProductDetailMapper,
) : ViewModel() {

  private val _sideEffect = Channel<ProductDetailSideEffect>(Channel.BUFFERED)
  val sideEffect: Flow<ProductDetailSideEffect> = _sideEffect.receiveAsFlow()

  val state: StateFlow<ProductDetailState> = obtainProductDetailUseCase
    .obtainProductDetail(productId)
    .map { result ->
      result.fold(
        onSuccess = { productWithInstances ->
          productDetailMapper.mapToProductDetail(productWithInstances)
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

  fun onConsumeInstance(instance: ProductInstanceDetailUiModel) {
    // TODO: Implement consume instance logic
  }

  fun onFreezeInstance(instance: ProductInstanceDetailUiModel) {
    // Check if instance is expired
    if (instance.status == InstanceStatus.Expired) {
      emitSideEffect(ProductDetailSideEffect.FreezeNotAvailable(instance.status))
      return
    }

    // TODO: Implement freeze instance logic
  }

  fun onDeleteInstance(instance: ProductInstanceDetailUiModel) {
    // TODO: Implement delete instance logic
  }

  private fun emitSideEffect(effect: ProductDetailSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }
}
