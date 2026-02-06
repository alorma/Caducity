package com.alorma.caducity.ui.screen.category.detail.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProductPageViewModel(
  private val categoryId: String,
  private val productId: String?,
) : ViewModel() {

  private val _state = MutableStateFlow<ProductPageState>(ProductPageState.Loading)
  val state: StateFlow<ProductPageState> = _state

  init {
    viewModelScope.launch {
      // Generate a random UUID for this product page
      val uuid = UUID.randomUUID().toString()
      _state.emit(ProductPageState.Success(uuid = uuid))
    }
  }
}
