package com.alorma.caducity.ui.screen.product.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ProductDetailViewModel(
  productId: String,
  obtainProductDetailUseCase: ObtainProductDetailUseCase,
  productDetailMapper: ProductDetailMapper,
  private val appClock: AppClock,
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
    when (instance.status) {
      InstanceStatus.ExpiringSoon -> {
        // Only show warning if expiration date is today
        val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        if (instance.expirationDate == today) {
          emitSideEffect(ProductDetailSideEffect.ShowConsumeExpiredWarning(instance))
        } else {
          onConsumeInstanceConfirmed(instance)
        }
      }
      InstanceStatus.Expired -> {
        // Show error dialog for expired items
        emitSideEffect(ProductDetailSideEffect.ShowConsumeExpiredError(instance, instance.status))
      }
      InstanceStatus.Fresh -> {
        onConsumeInstanceConfirmed(instance)
      }
      InstanceStatus.Consumed,
      InstanceStatus.Frozen -> {
        // Already consumed or frozen, no action needed
      }
    }
  }

  fun onConsumeInstanceConfirmed(instance: ProductInstanceDetailUiModel) {
    // TODO: Implement consume instance logic (called after user confirms warning)
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
