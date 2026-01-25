package com.alorma.caducity.ui.screen.settings.debug

import androidx.lifecycle.ViewModel
import com.alorma.caducity.feature.notification.NotificationDebugHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Debug Settings screen
 * Manages state for fake data generation and notifications
 */
class DebugSettingsViewModel(
  private val notificationDebugHelper: NotificationDebugHelper
) : ViewModel() {

  private val _uiState = MutableStateFlow(DebugSettingsUiState())
  val uiState: StateFlow<DebugSettingsUiState> = _uiState.asStateFlow()

  private val _sideEffect = MutableSharedFlow<DebugSettingsSideEffect>()
  val sideEffect: SharedFlow<DebugSettingsSideEffect> = _sideEffect.asSharedFlow()

  fun onTriggerNotificationCheck() {
    notificationDebugHelper.triggerImmediateCheck()
  }

  fun dismissError() {
    _uiState.value = _uiState.value.copy(error = null)
  }
}

/**
 * UI state for Debug Settings screen
 */
data class DebugSettingsUiState(
  val error: String? = null,
)

/**
 * Side effects for Debug Settings screen
 */
sealed interface DebugSettingsSideEffect
