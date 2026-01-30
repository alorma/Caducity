package com.alorma.caducity.ui.screen.category.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.usecase.AddItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.GetCategoryProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.time.Instant

class CategoryDetailAddItemViewModel(
  private val categoryId: String,
  private val preselectedProductId: String?,
  private val getCategoryProductsUseCase: GetCategoryProductsUseCase,
  private val createProductUseCase: CreateProductUseCase,
  private val addItemToCategoryUseCase: AddItemToCategoryUseCase,
  private val stringProvider: StringProvider,
  private val appClock: AppClock,
) : ViewModel() {

  private val _state = MutableStateFlow<CategoryDetailAddItemState>(
    CategoryDetailAddItemState.Loading
  )
  val state: StateFlow<CategoryDetailAddItemState> = _state.asStateFlow()

  private val _formState = MutableStateFlow(FormState())
  val formState: StateFlow<FormState> = _formState.asStateFlow()

  private var allProducts: List<ProductUiModel> = emptyList()

  init {
    loadProducts()
  }

  private fun loadProducts() {
    viewModelScope.launch {
      _state.value = CategoryDetailAddItemState.Loading
      getCategoryProductsUseCase(categoryId)
        .collect { products ->
          allProducts = products.map { product ->
            ProductUiModel(
              id = product.id,
              name = product.name,
            )
          }
          _state.value = CategoryDetailAddItemState.Success(
            products = allProducts
          )

          // Pre-select product if provided
          if (preselectedProductId != null) {
            val preselectedProduct = allProducts.find { it.id == preselectedProductId }
            if (preselectedProduct != null) {
              onProductSelected(preselectedProduct.id, preselectedProduct.name)
            }
          }
        }
    }
  }

  fun onProductTextChanged(text: TextFieldValue) {
    _formState.value = _formState.value.copy(
      productText = text,
      selectedProductId = null // Clear selection when user types
    )
  }

  fun onProductSelected(productId: String, productName: String) {
    _formState.value = _formState.value.copy(
      productText = TextFieldValue(
        text = productName,
        selection = TextRange(productName.length),
      ),
      selectedProductId = productId
    )
  }

  fun getFilteredProducts(): List<ProductUiModel> {
    val query = _formState.value.productText.text.lowercase()
    if (query.isEmpty()) return allProducts
    return allProducts.filter { it.name.lowercase().contains(query) }
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
      val productText = currentFormState.productText.text.trim()
      val identifierText = currentFormState.identifierText.text.trim()

      // Validation: Either product or identifier must be provided
      if (productText.isEmpty() && identifierText.isEmpty()) {
        _formState.value = currentFormState.copy(
          identifierError = "Either product or identifier must be provided"
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
        // Determine product ID (use existing or create new)
        val productId = if (productText.isEmpty()) {
          null
        } else if (currentFormState.selectedProductId != null) {
          currentFormState.selectedProductId
        } else {
          // Create new product
          val result = createProductUseCase.create(categoryId, productText)
          result.getOrThrow().id
        }

        // Convert selected date from milliseconds to Instant
        val expirationDate = Instant.fromEpochMilliseconds(currentFormState.expirationDateMillis)

        // Create multiple items
        repeat(quantity) { index ->
          // Determine identifier for each item
          val identifier = if (identifierText.isEmpty()) {
            // Empty identifier: auto-generate localized "Item #X"
            val itemNumber = index + 1
            val itemLabel = stringProvider.getString(R.string.product_detail_auto_identifier_item)
            "$itemLabel #$itemNumber"
          } else if (quantity > 1) {
            // Multiple items with provided identifier: append " - N"
            "$identifierText - ${index + 1}"
          } else {
            // Single item with provided identifier: use as-is
            identifierText
          }

          addItemToCategoryUseCase.addItem(
            categoryId = categoryId,
            identifier = identifier,
            productId = productId,
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
  val productText: TextFieldValue = TextFieldValue(),
  val selectedProductId: String? = null,
  val identifierText: TextFieldValue = TextFieldValue(),
  val identifierError: String? = null,
  val expirationDateMillis: Long? = null,
  val expirationDateError: String? = null,
  val quantity: Int = 1,
  val showCustomQuantityInput: Boolean = false,
  val customQuantity: TextFieldValue = TextFieldValue(),
)

sealed interface CategoryDetailAddItemState {
  data object Loading : CategoryDetailAddItemState
  data class Success(val products: List<ProductUiModel>) : CategoryDetailAddItemState
  data class Error(val message: String) : CategoryDetailAddItemState
}
