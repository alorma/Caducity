package com.alorma.caducity.ui.screen.settings.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.remoteconfig.ExampleFeatureConfig
import com.alorma.caducity.config.remoteconfig.ExampleMessageConfig
import com.alorma.caducity.config.remoteconfig.ExampleNumberConfig
import com.alorma.caducity.config.remoteconfig.RemoteConfigRunner
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
  private val remoteConfigRunner: RemoteConfigRunner,
  private val exampleFeatureConfig: ExampleFeatureConfig,
  private val exampleMessageConfig: ExampleMessageConfig,
  private val exampleNumberConfig: ExampleNumberConfig,
) : ViewModel() {

  private val _uiState = MutableStateFlow(DebugSettingsUiState())
  val uiState: StateFlow<DebugSettingsUiState> = _uiState.asStateFlow()

  private val _sideEffect = MutableSharedFlow<DebugSettingsSideEffect>()
  val sideEffect: SharedFlow<DebugSettingsSideEffect> = _sideEffect.asSharedFlow()

  init {
    // Load current remote config values
    loadRemoteConfigValues()
  }

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

  fun onRefreshRemoteConfig() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isRefreshingRemoteConfig = true)

      remoteConfigRunner.fetchAndActivate()
        .onSuccess { activated ->
          _sideEffect.emit(
            DebugSettingsSideEffect.RemoteConfigRefreshed(activated)
          )
          loadRemoteConfigValues()
          _uiState.value = _uiState.value.copy(isRefreshingRemoteConfig = false)
        }
        .onFailure { error ->
          _uiState.value = _uiState.value.copy(
            isRefreshingRemoteConfig = false,
            error = error.message ?: "Failed to refresh remote config"
          )
        }
    }
  }

  private fun loadRemoteConfigValues() {
    _uiState.value = _uiState.value.copy(
      remoteConfigValues = mapOf(
        exampleFeatureConfig.key to exampleFeatureConfig.isEnabled().toString(),
        exampleMessageConfig.key to exampleMessageConfig.asString(),
        exampleNumberConfig.key to exampleNumberConfig.asLong().toString(),
      )
    )
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
  val isRefreshingRemoteConfig: Boolean = false,
  val error: String? = null,
  val remoteConfigValues: Map<String, String> = emptyMap(),
)

/**
 * Side effects for Debug Settings screen
 */
sealed interface DebugSettingsSideEffect {
  data object FakeDataPopulated : DebugSettingsSideEffect
  data class RemoteConfigRefreshed(val activated: Boolean) : DebugSettingsSideEffect
}
