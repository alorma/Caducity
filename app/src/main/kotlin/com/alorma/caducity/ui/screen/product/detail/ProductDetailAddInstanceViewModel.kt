package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.GetProductVariantsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailAddInstanceViewModel(
  private val productId: String,
  private val getProductVariantsUseCase: GetProductVariantsUseCase,
) : ViewModel() {

  private val _state =
    MutableStateFlow<ProductDetailAddInstanceState>(ProductDetailAddInstanceState.Loading)
  val state: StateFlow<ProductDetailAddInstanceState> = _state.asStateFlow()

  private val _formState = MutableStateFlow(FormState())
  val formState: StateFlow<FormState> = _formState.asStateFlow()

  private var allVariants: List<VariantUiModel> = emptyList()

  init {
    loadVariants()
  }

  private fun loadVariants() {
    viewModelScope.launch {
      _state.value = ProductDetailAddInstanceState.Loading
      getProductVariantsUseCase(productId)
        .collect { variants ->
          allVariants = variants.map { variant ->
            VariantUiModel(
              id = variant.id,
              name = variant.name,
            )
          }
          _state.value = ProductDetailAddInstanceState.Success(
            variants = allVariants
          )
        }
    }
  }

  fun onVariantTextChanged(text: TextFieldValue) {
    _formState.value = _formState.value.copy(
      variantText = text,
      selectedVariantId = null // Clear selection when user types
    )
  }

  fun onVariantSelected(variantId: String, variantName: String) {
    _formState.value = _formState.value.copy(
      variantText = TextFieldValue(
        text = variantName,
        selection = TextRange(variantName.length),
      ),
      selectedVariantId = variantId
    )
  }

  fun getFilteredVariants(): List<VariantUiModel> {
    val query = _formState.value.variantText.text.lowercase()
    if (query.isEmpty()) return allVariants
    return allVariants.filter { it.name.lowercase().contains(query) }
  }
}

data class FormState(
  val variantText: TextFieldValue = TextFieldValue(),
  val selectedVariantId: String? = null,
)

sealed interface ProductDetailAddInstanceState {
  data object Loading : ProductDetailAddInstanceState
  data class Success(val variants: List<VariantUiModel>) : ProductDetailAddInstanceState
  data class Error(val message: String) : ProductDetailAddInstanceState
}
