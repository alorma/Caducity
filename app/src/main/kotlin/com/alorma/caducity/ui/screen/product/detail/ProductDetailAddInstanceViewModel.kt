package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.usecase.AddInstanceToProductUseCase
import com.alorma.caducity.domain.usecase.CreateVariantUseCase
import com.alorma.caducity.domain.usecase.GetProductVariantsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.time.Instant

class ProductDetailAddInstanceViewModel(
  private val productId: String,
  private val getProductVariantsUseCase: GetProductVariantsUseCase,
  private val createVariantUseCase: CreateVariantUseCase,
  private val addInstanceToProductUseCase: AddInstanceToProductUseCase,
  private val productDataSource: ProductDataSource,
  private val stringProvider: StringProvider,
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

  fun onExpirationDateChanged(dateMillis: Long?) {
    _formState.value = _formState.value.copy(
      expirationDateMillis = dateMillis,
      expirationDateError = null // Clear error when user selects date
    )
  }

  fun onQuantityChanged(quantity: Int) {
    _formState.value = _formState.value.copy(quantity = quantity)
  }

  fun onShowCustomQuantityInputChanged(show: Boolean) {
    _formState.value = _formState.value.copy(
      showCustomQuantityInput = show,
      customQuantity = if (!show) TextFieldValue() else _formState.value.customQuantity
    )
  }

  fun onCustomQuantityChanged(text: TextFieldValue) {
    _formState.value = _formState.value.copy(customQuantity = text)
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

      // Validation: Expiration date must be provided
      if (currentFormState.expirationDateMillis == null) {
        _formState.value = currentFormState.copy(
          expirationDateError = "Expiration date is required"
        )
        return@launch
      }

      // Determine quantity
      val quantity = if (currentFormState.showCustomQuantityInput && currentFormState.customQuantity.text.isNotBlank()) {
        currentFormState.customQuantity.text.toIntOrNull()?.coerceAtLeast(1) ?: 1
      } else {
        currentFormState.quantity
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

        // Get existing instance count for this variant/standalone
        val productWithInstances = productDataSource.getProduct(productId).firstOrNull()?.getOrNull()
        val existingInstanceCount = if (variantId != null) {
          // Count instances in the specific variant
          productWithInstances?.variants
            ?.find { it.variant.id == variantId }
            ?.instances?.size ?: 0
        } else {
          // Count standalone instances (no variant)
          productWithInstances?.standaloneInstances?.size ?: 0
        }

        // Convert selected date from milliseconds to Instant
        val expirationDate = Instant.fromEpochMilliseconds(currentFormState.expirationDateMillis)

        // Create multiple instances
        repeat(quantity) { index ->
          // Determine identifier for each instance
          val identifier = if (identifierText.isEmpty()) {
            // Empty identifier: auto-generate localized "Item #X"
            val itemNumber = existingInstanceCount + index + 1
            val itemLabel = stringProvider.getString(R.string.product_detail_auto_identifier_item)
            "$itemLabel #$itemNumber"
          } else if (quantity > 1) {
            // Multiple instances with provided identifier: append " - N"
            "$identifierText - ${index + 1}"
          } else {
            // Single instance with provided identifier: use as-is
            identifierText
          }

          addInstanceToProductUseCase.addInstance(
            productId = productId,
            identifier = identifier,
            variantId = variantId,
            expirationDate = expirationDate,
          )
        }

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
  val expirationDateMillis: Long? = null,
  val expirationDateError: String? = null,
  val quantity: Int = 1,
  val showCustomQuantityInput: Boolean = false,
  val customQuantity: TextFieldValue = TextFieldValue(),
)

sealed interface ProductDetailAddInstanceState {
  data object Loading : ProductDetailAddInstanceState
  data class Success(val variants: List<VariantUiModel>) : ProductDetailAddInstanceState
  data class Error(val message: String) : ProductDetailAddInstanceState
}
