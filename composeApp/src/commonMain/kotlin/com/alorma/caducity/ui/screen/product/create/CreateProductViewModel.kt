package com.alorma.caducity.ui.screen.product.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

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

  fun updateExpirationDate(date: String) {
    _state.update { it.copy(expirationDateText = date) }
  }

  fun createProduct(onSuccess: () -> Unit) {
    val currentState = _state.value

    if (!validateInput(currentState)) {
      return
    }

    _state.update { it.copy(isLoading = true, error = null) }

    viewModelScope.launch {
      val expirationDate = parseDate(currentState.expirationDateText)
      if (expirationDate == null) {
        _state.update {
          it.copy(
            isLoading = false,
            error = "Invalid date format. Please use DD/MM/YYYY"
          )
        }
        return@launch
      }

      val instant = expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault())

      val result = createProductUseCase.createProduct(
        name = currentState.name,
        description = currentState.description,
        expirationDate = instant,
      )

      result.fold(
        onSuccess = {
          _state.update {
            CreateProductState()
          }
          onSuccess()
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
    if (state.expirationDateText.isBlank()) {
      _state.update { it.copy(error = "Expiration date is required") }
      return false
    }
    return true
  }

  private fun parseDate(dateText: String): LocalDate? {
    return try {
      val parts = dateText.split("/")
      if (parts.size != 3) return null

      val day = parts[0].toIntOrNull() ?: return null
      val month = parts[1].toIntOrNull() ?: return null
      val year = parts[2].toIntOrNull() ?: return null

      LocalDate(year, month, day)
    } catch (e: Exception) {
      null
    }
  }

  fun clearError() {
    _state.update { it.copy(error = null) }
  }
}

data class CreateProductState(
  val name: String = "",
  val description: String = "",
  val expirationDateText: String = "",
  val isLoading: Boolean = false,
  val error: String? = null,
)
