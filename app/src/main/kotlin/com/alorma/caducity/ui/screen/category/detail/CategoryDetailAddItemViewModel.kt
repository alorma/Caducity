package com.alorma.caducity.ui.screen.category.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.AddItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.GetCategoryProductsUseCase
import com.alorma.caducity.feature.tracking.CancelAddItemAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemSavedAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

class CategoryDetailAddItemViewModel(
  private val categoryId: String,
  private val preSelectedProductId: String?,
  private val getCategoryProductsUseCase: GetCategoryProductsUseCase,
  private val createProductUseCase: CreateProductUseCase,
  private val addItemToCategoryUseCase: AddItemToCategoryUseCase,
  private val stringProvider: StringProvider,
  private val eventTracker: EventTracker,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) : BaseViewModel<AddItemNavigation, AddItemNavigationSideEffect, AddItemSideEffect>() {

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
      getCategoryProductsUseCase.obtain(categoryId)
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
          if (preSelectedProductId != null) {
            val preselectedProduct = allProducts.find { it.id == preSelectedProductId }
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
      selectedcategoryId = null // Clear selection when user types
    )
  }

  fun onProductSelected(categoryId: String, productName: String) {
    _formState.value = _formState.value.copy(
      productText = TextFieldValue(
        text = productName,
        selection = TextRange(productName.length),
      ),
      selectedcategoryId = categoryId
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

  fun onShowDatePicker() {
    emitSideEffect(AddItemSideEffect.ShowDatePicker(_formState.value.expirationDateMillis))
  }

  fun calculateStatusForDate(dateMillis: Long?): AppFeedbackType {
    if (dateMillis == null) return AppFeedbackType.Info

    val selectedDate = Instant.fromEpochMilliseconds(dateMillis)
    return ItemStatus.calculateStatus(
      expirationDate = selectedDate,
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold
    ).let {
      AppFeedbackType.Status(it)
    }
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

  fun onIsPackChanged(isPack: Boolean) {
    _formState.value = _formState.value.copy(
      isPack = isPack,
      packSize = if (!isPack) TextFieldValue() else _formState.value.packSize
    )
  }

  fun onPackSizeChanged(text: TextFieldValue) {
    _formState.value = _formState.value.copy(packSize = text)
  }

  fun save() {
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
      val quantity =
        if (currentFormState.showCustomQuantityInput && currentFormState.customQuantity.text.isNotBlank()) {
          currentFormState.customQuantity.text.toIntOrNull()?.coerceAtLeast(1) ?: 1
        } else {
          currentFormState.quantity
        }

      // Determine pack size
      val packSize = if (currentFormState.isPack && currentFormState.packSize.text.isNotBlank()) {
        currentFormState.packSize.text.toIntOrNull()?.coerceAtLeast(2)
      } else {
        null
      }

      try {
        // Determine product ID (use existing or create new)
        val productId = if (productText.isEmpty()) {
          null
        } else if (currentFormState.selectedcategoryId != null) {
          currentFormState.selectedcategoryId
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
            val itemLabel = stringProvider.getString(R.string.category_detail_auto_identifier_item)
            "$itemLabel #$itemNumber"
          } else if (quantity > 1) {
            // Multiple items with provided identifier: append " - N"
            "$identifierText - ${index + 1}"
          } else {
            // Single item with provided identifier: use as-is
            identifierText
          }

          addItemToCategoryUseCase.addItem(
            categoryId = this@CategoryDetailAddItemViewModel.categoryId,
            identifier = identifier,
            productId = productId,
            expirationDate = expirationDate,
            packSize = packSize,
          )
        }

        navigate(AddItemNavigation.ItemSaved(hasProduct = productId != null, quantity = quantity))
      } catch (e: Exception) {
        // TODO: Handle error
        e.printStackTrace()
      }
    }
  }

  override fun navigate(navigation: AddItemNavigation) {
    when (navigation) {
      AddItemNavigation.Cancel -> {
        eventTracker.trackAction(CancelAddItemAction())
        emitNavigationSideEffect(AddItemNavigationSideEffect.NavigateBack)
      }

      is AddItemNavigation.ItemSaved -> {
        eventTracker.trackAction(ItemSavedAction(navigation.hasProduct, navigation.quantity))
        emitNavigationSideEffect(AddItemNavigationSideEffect.NavigateBack)
      }
    }
  }
}

data class FormState(
  val productText: TextFieldValue = TextFieldValue(),
  val selectedcategoryId: String? = null,
  val identifierText: TextFieldValue = TextFieldValue(),
  val identifierError: String? = null,
  val expirationDateMillis: Long? = null,
  val expirationDateError: String? = null,
  val quantity: Int = 1,
  val showCustomQuantityInput: Boolean = false,
  val customQuantity: TextFieldValue = TextFieldValue(),
  val isPack: Boolean = false,
  val packSize: TextFieldValue = TextFieldValue(),
)

sealed interface CategoryDetailAddItemState {
  data object Loading : CategoryDetailAddItemState
  data class Success(val products: List<ProductUiModel>) : CategoryDetailAddItemState
  data class Error(val message: String) : CategoryDetailAddItemState
}
