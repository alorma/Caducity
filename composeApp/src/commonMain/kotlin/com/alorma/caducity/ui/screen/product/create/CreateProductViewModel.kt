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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

  fun updateInstanceIdentifier(instanceId: String, identifier: String) {
    _state.update { currentState ->
      val updatedInstances = currentState.instances.map { instance ->
        if (instance.id == instanceId) {
          instance.copy(identifier = identifier)
        } else {
          instance
        }
      }
      currentState.copy(instances = updatedInstances)
    }
  }

  fun updateInstanceExpirationDate(instanceId: String, date: LocalDate) {
    _state.update { currentState ->
      val updatedInstances = currentState.instances.map { instance ->
        if (instance.id == instanceId) {
          instance.copy(
            expirationDateText = dateFormat.format(date),
            expirationDate = date,
          )
        } else {
          instance
        }
      }
      currentState.copy(instances = updatedInstances)
    }
  }

  fun showDatePickerForInstance(instanceId: String) {
    _state.update { it.copy(showDatePickerForInstanceId = instanceId) }
  }

  fun hideDatePicker() {
    _state.update { it.copy(showDatePickerForInstanceId = null) }
  }

  @OptIn(ExperimentalUuidApi::class)
  fun addInstance() {
    _state.update { currentState ->
      val newInstance = ProductInstanceInput(
        id = Uuid.random().toString()
      )
      currentState.copy(instances = currentState.instances + newInstance)
    }
  }

  fun removeInstance(instanceId: String) {
    _state.update { currentState ->
      if (currentState.instances.size > 1) {
        currentState.copy(instances = currentState.instances.filter { it.id != instanceId })
      } else {
        currentState
      }
    }
  }

  fun createProduct(onSuccess: () -> Unit) {
    val currentState = _state.value

    if (!validateInput(currentState)) {
      return
    }

    _state.update { it.copy(isLoading = true, error = null) }

    viewModelScope.launch {
      val instancesData = currentState.instances.map { instance ->
        if (instance.expirationDate == null) {
          _state.update {
            it.copy(
              isLoading = false,
              error = "All instances must have an expiration date"
            )
          }
          return@launch
        }

        val instant = instance.expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        instance.identifier to instant
      }

      val result = createProductUseCase.createProduct(
        name = currentState.name,
        description = currentState.description,
        instances = instancesData,
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
    if (state.instances.isEmpty()) {
      _state.update { it.copy(error = "At least one instance is required") }
      return false
    }
    if (state.instances.any { it.expirationDate == null }) {
      _state.update { it.copy(error = "All instances must have an expiration date") }
      return false
    }
    if (state.instances.any { it.identifier.isBlank() }) {
      _state.update { it.copy(error = "All instances must have an identifier") }
      return false
    }
    return true
  }

  fun clearError() {
    _state.update { it.copy(error = null) }
  }

  fun initializeWithDate(date: LocalDate) {
    if (_state.value.instances.isEmpty()) {
      addInstance()
      val newInstance = _state.value.instances.first()
      updateInstanceExpirationDate(newInstance.id, date)
    }
  }
}


