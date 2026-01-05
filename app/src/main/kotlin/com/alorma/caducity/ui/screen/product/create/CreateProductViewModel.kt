package com.alorma.caducity.ui.screen.product.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.DateTimeFormat
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateProductViewModel(
  private val createProductUseCase: CreateProductUseCase,
) : ViewModel() {

  private val _state = MutableStateFlow(CreateProductState())
  val state: StateFlow<CreateProductState> = _state.asStateFlow()

  fun updateName(name: String) {
    _state.update { it.copy(name = name) }
  }

  fun updateDescription(description: String) {
    _state.update { it.copy(description = description) }
  }

  fun createProduct(onSuccess: (String) -> Unit) {
    val currentState = _state.value

    if (!validateInput(currentState)) {
      return
    }

    _state.update { it.copy(isLoading = true, error = null) }

    viewModelScope.launch {
      val result = createProductUseCase.createProduct(
        name = currentState.name,
        description = currentState.description,
        instances = emptyList(), // No instances on creation
      )

      result.fold(
        onSuccess = { productId ->
          _state.update { CreateProductState() }
          onSuccess(productId)
        },
        onFailure = { error ->
          _state.update {
            it.copy(
              isLoading = false,
              error = error.message ?: "Failed to create product"
            )
          }
        }
      )
    }
  }

  private fun validateInput(state: CreateProductState): Boolean {
    if (state.name.isBlank()) {
      _state.update { it.copy(error = "Product name is required") }
      return false
    }
    return true
  }

  fun clearError() {
    _state.update { it.copy(error = null) }
  }
}


