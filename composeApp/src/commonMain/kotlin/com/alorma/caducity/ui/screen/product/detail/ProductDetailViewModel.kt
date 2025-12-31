package com.alorma.caducity.ui.screen.product.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.AddInstanceToProductUseCase
import com.alorma.caducity.domain.usecase.ObtainProductDetailUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class ProductDetailViewModel(
  private val productId: String,
  obtainProductDetailUseCase: ObtainProductDetailUseCase,
  productDetailMapper: ProductDetailMapper,
  private val addInstanceToProductUseCase: AddInstanceToProductUseCase,
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

  fun addInstance(
    identifier: String,
    expirationDate: LocalDate,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      val instant = expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault())
      val result = addInstanceToProductUseCase.addInstance(
        productId = productId,
        identifier = identifier,
        expirationDate = instant,
      )

      result.fold(
        onSuccess = {
          onSuccess()
        },
        onFailure = { error ->
          // Error handling can be enhanced later if needed
          println("Failed to add instance: ${error.message}")
        }
      )
    }
  }
}
