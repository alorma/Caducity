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
import kotlinx.datetime.format.DateTimeFormat

class CreateProductViewModel(
  private val createProductUseCase: CreateProductUseCase,
  private val dateFormat: DateTimeFormat<LocalDate>,
  private val selectableDates: FutureDateSelectableDates,
) : ViewModel() {

  private val _state = MutableStateFlow(
    CreateProductState(selectableDates = selectableDates)
  )
  val state: StateFlow<CreateProductState> = _state.asStateFlow()

  fun updateName(name: String) {
    _state.update { it.copy(name = name) }
  }

  fun updateDescription(description: String) {
    _state.update { it.copy(description = description) }
  }

  fun updateExpirationDate(date: LocalDate) {
    _state.update {
      it.copy(
        expirationDateText = dateFormat.format(date),
        expirationDate = date,
      )
    }
  }

  fun showDatePicker() {
    _state.update { it.copy(showDatePicker = true) }
  }

  fun hideDatePicker() {
    _state.update { it.copy(showDatePicker = false) }
  }

  fun createProduct(onSuccess: () -> Unit) {
    val currentState = _state.value

    if (!validateInput(currentState)) {
      return
    }

    _state.update { it.copy(isLoading = true, error = null) }

    viewModelScope.launch {
      val expirationDate = currentState.expirationDate
      if (expirationDate == null) {
        _state.update {
          it.copy(
            isLoading = false,
            error = "Expiration date is required"
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
            CreateProductState(selectableDates = selectableDates)
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
    if (state.expirationDate == null) {
      _state.update { it.copy(error = "Expiration date is required") }
      return false
    }
    return true
  }

  fun clearError() {
    _state.update { it.copy(error = null) }
  }
}


