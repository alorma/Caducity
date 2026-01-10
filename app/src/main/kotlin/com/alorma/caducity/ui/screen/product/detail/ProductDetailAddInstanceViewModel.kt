package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.usecase.AddInstanceToProductUseCase
import com.alorma.caducity.domain.usecase.CreateVariantUseCase
import com.alorma.caducity.domain.usecase.GetProductVariantsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

class ProductDetailAddInstanceViewModel(
  private val productId: String,
  private val getProductVariantsUseCase: GetProductVariantsUseCase,
  private val createVariantUseCase: CreateVariantUseCase,
  private val addInstanceToProductUseCase: AddInstanceToProductUseCase,
  private val appClock: AppClock,
) : ViewModel() {

  private val _state = MutableStateFlow<ProductDetailAddInstanceState>(
    ProductDetailAddInstanceState.Loading
  )
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

  fun onIdentifierTextChanged(text: TextFieldValue) {
    _formState.value = _formState.value.copy(
      identifierText = text,
      identifierError = null // Clear error when user types
    )
  }

  fun save(onSuccess: () -> Unit) {
    viewModelScope.launch {
      val currentFormState = _formState.value
      val variantText = currentFormState.variantText.text.trim()
      val identifierText = currentFormState.identifierText.text.trim()

      // Validation: Either variant or identifier must be provided
      if (variantText.isEmpty() && identifierText.isEmpty()) {
        _formState.value = currentFormState.copy(
          identifierError = "Either variant or identifier must be provided"
        )
        return@launch
      }

      try {
        // Determine variant ID (use existing or create new)
        val variantId = if (variantText.isEmpty()) {
          null
        } else if (currentFormState.selectedVariantId != null) {
          currentFormState.selectedVariantId
        } else {
          // Create new variant
          val result = createVariantUseCase.create(productId, variantText)
          result.getOrThrow().id
        }

        // Determine identifier
        val identifier = if (variantId != null) {
          // Variant selected: identifier can be empty or use provided value
          identifierText.ifEmpty { "" }
        } else {
          // No variant: use provided identifier (we validated it's not empty above)
          identifierText
        }

        // Create instance with data
        val fakeExpirationDate = appClock.now().minus(4.days)

        addInstanceToProductUseCase.addInstance(
          productId = productId,
          identifier = identifier,
          variantId = variantId,
          expirationDate = fakeExpirationDate,
        )

        onSuccess()
      } catch (e: Exception) {
        // TODO: Handle error
        e.printStackTrace()
      }
    }
  }
}

data class FormState(
  val variantText: TextFieldValue = TextFieldValue(),
  val selectedVariantId: String? = null,
  val identifierText: TextFieldValue = TextFieldValue(),
  val identifierError: String? = null,
)

sealed interface ProductDetailAddInstanceState {
  data object Loading : ProductDetailAddInstanceState
  data class Success(val variants: List<VariantUiModel>) : ProductDetailAddInstanceState
  data class Error(val message: String) : ProductDetailAddInstanceState
}
