package com.alorma.caducity.ui.screen.category.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.feature.tracking.CancelCreateCategoryAction
import com.alorma.caducity.feature.tracking.CategoryCreatedAction
import com.alorma.caducity.feature.tracking.EventTracker
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.DateTimeFormat
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateCategoryViewModel(
  private val createCategoryUseCase: CreateCategoryUseCase,
  private val eventTracker: EventTracker,
) : ViewModel() {

  private val _state = MutableStateFlow(CreateCategoryState())
  val state: StateFlow<CreateCategoryState> = _state.asStateFlow()

  private val navigationSideEffectChannel = Channel<CreateCategoryNavigationSideEffect>()
  val navigationSideEffects = navigationSideEffectChannel.receiveAsFlow()

  fun updateName(name: String) {
    _state.update { it.copy(name = name) }
  }

  fun updateDescription(description: String) {
    _state.update { it.copy(description = description) }
  }

  fun createCategory() {
    val currentState = _state.value

    if (!validateInput(currentState)) {
      return
    }

    _state.update { it.copy(isLoading = true, error = null) }

    viewModelScope.launch {
      val result = createCategoryUseCase.createCategory(
        name = currentState.name,
        description = currentState.description,
        items = emptyList(), // No items on creation
      )

      result.fold(
        onSuccess = { categoryId ->
          _state.update { CreateCategoryState() }
          navigate(CreateCategoryNavigation.CategoryCreated(categoryId))
        },
        onFailure = { error ->
          _state.update {
            it.copy(
              isLoading = false,
              error = error.message ?: "Failed to create category"
            )
          }
        }
      )
    }
  }

  private fun validateInput(state: CreateCategoryState): Boolean {
    if (state.name.isBlank()) {
      _state.update { it.copy(error = "Category name is required") }
      return false
    }
    return true
  }

  fun clearError() {
    _state.update { it.copy(error = null) }
  }

  fun navigate(navigation: CreateCategoryNavigation) {
    when (navigation) {
      CreateCategoryNavigation.Cancel -> {
        eventTracker.trackAction(CancelCreateCategoryAction())
        emitNavigationSideEffect(CreateCategoryNavigationSideEffect.NavigateBack)
      }
      is CreateCategoryNavigation.CategoryCreated -> {
        eventTracker.trackAction(CategoryCreatedAction("form_submit"))
        emitNavigationSideEffect(
          CreateCategoryNavigationSideEffect.NavigateToCategoryDetail(navigation.categoryId)
        )
      }
    }
  }

  private fun emitNavigationSideEffect(effect: CreateCategoryNavigationSideEffect) {
    viewModelScope.launch {
      navigationSideEffectChannel.send(effect)
    }
  }
}


