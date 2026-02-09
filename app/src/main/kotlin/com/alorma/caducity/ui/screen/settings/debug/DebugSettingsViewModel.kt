package com.alorma.caducity.ui.screen.settings.debug

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.remoteconfig.DebugRemoteConfigRunner
import com.alorma.caducity.config.remoteconfig.RemoteConfig
import com.alorma.caducity.config.remoteconfig.RemoteConfigRunner
import com.alorma.caducity.domain.usecase.PopulateFakeDataUseCase
import com.alorma.caducity.domain.usecase.fakedata.FakePlayStoreDataStrategy
import com.alorma.caducity.domain.usecase.fakedata.FakeTestDataStrategy
import com.alorma.caducity.feature.consent.ConsentManager
import com.alorma.caducity.feature.consent.ConsentStatus
import com.alorma.caducity.feature.notification.NotificationDebugHelper
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Debug Settings screen
 * Manages state for fake data generation and notifications
 */
class DebugSettingsViewModel(
  private val notificationDebugHelper: NotificationDebugHelper,
  private val populateFakeDataUseCase: PopulateFakeDataUseCase,
  private val fakeTestDataStrategy: FakeTestDataStrategy,
  private val fakePlayStoreDataStrategy: FakePlayStoreDataStrategy,
  private val remoteConfigRunner: RemoteConfigRunner,
  private val remoteConfigs: List<RemoteConfig>,
  private val consentManager: ConsentManager,
) : BaseViewModel<Unit, DebugSettingsSideEffect, DebugSettingsSideEffect>() {

  private val _uiState = MutableStateFlow(DebugSettingsUiState())
  val uiState: StateFlow<DebugSettingsUiState> = _uiState.asStateFlow()

  init {
    // Load current remote config values
    loadRemoteConfigValues()
    // Load current consent preferences
    loadConsentPreferences()
  }

  fun onTriggerNotificationCheck() {
    notificationDebugHelper.triggerImmediateCheck()
  }

  fun onPopulateFakeData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isGenerating = true, error = null)

      val result = populateFakeDataUseCase.execute(fakeTestDataStrategy)

      result.fold(
        onSuccess = {
          emitSideEffect(DebugSettingsSideEffect.FakeDataPopulated)
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

  fun onPopulateFakePlayStoreData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isGeneratingPlayStore = true, error = null)

      val result = populateFakeDataUseCase.execute(fakePlayStoreDataStrategy)

      result.fold(
        onSuccess = {
          emitSideEffect(DebugSettingsSideEffect.FakePlayStoreDataPopulated)
          _uiState.value = _uiState.value.copy(isGeneratingPlayStore = false)
        },
        onFailure = { error ->
          _uiState.value = _uiState.value.copy(
            isGeneratingPlayStore = false,
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
          emitSideEffect(
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
    val debugRunner = remoteConfigRunner as? DebugRemoteConfigRunner

    _uiState.value = _uiState.value.copy(
      remoteConfigValues = remoteConfigs.associate { config ->
        config.key to RemoteConfigUiState(
          value = config.isEnabled(),
          hasDebugOverride = debugRunner?.hasDebugOverride(config.key) ?: false
        )
      }
    )
  }

  fun onToggleRemoteConfig(key: String, enabled: Boolean) {
    val debugRunner = remoteConfigRunner as? DebugRemoteConfigRunner
    if (debugRunner != null) {
      debugRunner.setDebugValue(key, enabled)
      loadRemoteConfigValues()
    }
  }

  fun onClearRemoteConfigOverride(key: String) {
    val debugRunner = remoteConfigRunner as? DebugRemoteConfigRunner
    if (debugRunner != null) {
      debugRunner.clearDebugValue(key)
      loadRemoteConfigValues()
    }
  }

  private fun loadConsentPreferences() {
    val preferences = consentManager.getConsentPreferences()
    _uiState.value = _uiState.value.copy(
      adStorageEnabled = preferences.adStorage == ConsentStatus.GRANTED,
      adUserDataEnabled = preferences.adUserData == ConsentStatus.GRANTED,
      adPersonalizationEnabled = preferences.adPersonalization == ConsentStatus.GRANTED,
    )
  }

  fun onToggleAdStorage(enabled: Boolean) {
    val current = consentManager.getConsentPreferences()
    val updated = current.copy(
      adStorage = if (enabled) ConsentStatus.GRANTED else ConsentStatus.DENIED
    )
    consentManager.setConsentPreferences(updated)
    loadConsentPreferences()
  }

  fun onToggleAdUserData(enabled: Boolean) {
    val current = consentManager.getConsentPreferences()
    val updated = current.copy(
      adUserData = if (enabled) ConsentStatus.GRANTED else ConsentStatus.DENIED
    )
    consentManager.setConsentPreferences(updated)
    loadConsentPreferences()
  }

  fun onToggleAdPersonalization(enabled: Boolean) {
    val current = consentManager.getConsentPreferences()
    val updated = current.copy(
      adPersonalization = if (enabled) ConsentStatus.GRANTED else ConsentStatus.DENIED
    )
    consentManager.setConsentPreferences(updated)
    loadConsentPreferences()
  }

  fun dismissError() {
    _uiState.value = _uiState.value.copy(error = null)
  }

  override fun navigate(navigation: Unit) {
    // Empty - this ViewModel doesn't navigate
  }
}

/**
 * UI state for a remote config value
 */
data class RemoteConfigUiState(
  val value: Boolean,
  val hasDebugOverride: Boolean,
)

/**
 * UI state for Debug Settings screen
 */
data class DebugSettingsUiState(
  val isGenerating: Boolean = false,
  val isGeneratingPlayStore: Boolean = false,
  val isRefreshingRemoteConfig: Boolean = false,
  val error: String? = null,
  val remoteConfigValues: Map<String, RemoteConfigUiState> = emptyMap(),
  val adStorageEnabled: Boolean = false,
  val adUserDataEnabled: Boolean = false,
  val adPersonalizationEnabled: Boolean = false,
)

/**
 * Side effects for Debug Settings screen
 */
sealed interface DebugSettingsSideEffect {
  data object FakeDataPopulated : DebugSettingsSideEffect
  data object FakePlayStoreDataPopulated : DebugSettingsSideEffect
  data class RemoteConfigRefreshed(val activated: Boolean) : DebugSettingsSideEffect
}
