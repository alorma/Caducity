package com.alorma.caducity.ui.screen.settings.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.PopulateFakeDataUseCase
import com.alorma.caducity.feature.notification.NotificationDebugHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Debug Settings screen
 * Manages state for fake data generation and notifications
 */
class DebugSettingsViewModel(
  private val notificationDebugHelper: NotificationDebugHelper,
  private val populateFakeDataUseCase: PopulateFakeDataUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(DebugSettingsUiState())
  val uiState: StateFlow<DebugSettingsUiState> = _uiState.asStateFlow()

  private val _sideEffect = MutableSharedFlow<DebugSettingsSideEffect>()
  val sideEffect: SharedFlow<DebugSettingsSideEffect> = _sideEffect.asSharedFlow()

  fun onTriggerNotificationCheck() {
    notificationDebugHelper.triggerImmediateCheck()
  }

  fun onPopulateFakeData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isGenerating = true, error = null)

      val result = populateFakeDataUseCase.execute()

      result.fold(
        onSuccess = {
          _sideEffect.emit(DebugSettingsSideEffect.FakeDataPopulated)
          _uiState.value = _uiState.value.copy(isGenerating = false)
        },
        onFailure = { error ->
          _uiState.value = _uiState.value.copy(
            isGenerating = false,
            error = error.message ?: "Unknown error occurred"
          )
        }
      )
    }
  }

  fun dismissError() {
    _uiState.value = _uiState.value.copy(error = null)
  }
}

/**
 * UI state for Debug Settings screen
 */
data class DebugSettingsUiState(
  val isGenerating: Boolean = false,
  val error: String? = null,
)

/**
 * Side effects for Debug Settings screen
 */
sealed interface DebugSettingsSideEffect {
  data object FakeDataPopulated : DebugSettingsSideEffect
}
