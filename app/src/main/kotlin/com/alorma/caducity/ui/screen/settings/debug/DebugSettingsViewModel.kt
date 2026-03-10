package com.alorma.caducity.ui.screen.settings.debug

import androidx.lifecycle.viewModelScope
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
  private val consentManager: ConsentManager,
) : BaseViewModel<Unit, DebugSettingsSideEffect, DebugSettingsSideEffect>() {
  private val _uiState =
    MutableStateFlow(
      DebugSettingsUiState(
        onPopulateFakeData = ::onPopulateFakeData,
        onPopulateFakePlayStoreData = ::onPopulateFakePlayStoreData,
        onTriggerNotificationCheck = ::onTriggerNotificationCheck,
        onToggleAdStorage = ::onToggleAdStorage,
        onToggleAdUserData = ::onToggleAdUserData,
        onToggleAdPersonalization = ::onToggleAdPersonalization,
      ),
    )
  val uiState: StateFlow<DebugSettingsUiState> = _uiState.asStateFlow()

  init {
    // Load current consent preferences
    loadConsentPreferences()
  }

  fun onTriggerNotificationCheck() {
    notificationDebugHelper.triggerImmediateCheck()
  }

  fun onPopulateFakeData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isGenerating = true)

      val result = populateFakeDataUseCase.execute(fakeTestDataStrategy)

      result.fold(
        onSuccess = {
          emitSideEffect(DebugSettingsSideEffect.FakeDataPopulated)
          _uiState.value = _uiState.value.copy(isGenerating = false)
        },
        onFailure = {
          emitSideEffect(DebugSettingsSideEffect.Error)
        },
      )
    }
  }

  fun onPopulateFakePlayStoreData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isGeneratingPlayStore = true)

      val result = populateFakeDataUseCase.execute(fakePlayStoreDataStrategy)

      result.fold(
        onSuccess = {
          emitSideEffect(DebugSettingsSideEffect.FakePlayStoreDataPopulated)
          _uiState.value = _uiState.value.copy(isGeneratingPlayStore = false)
        },
        onFailure = {
          emitSideEffect(DebugSettingsSideEffect.Error)
        },
      )
    }
  }

  private fun loadConsentPreferences() {
    val preferences = consentManager.getConsentPreferences()
    _uiState.value =
      _uiState.value.copy(
        adStorageEnabled = preferences.adStorage == ConsentStatus.GRANTED,
        adUserDataEnabled = preferences.adUserData == ConsentStatus.GRANTED,
        adPersonalizationEnabled = preferences.adPersonalization == ConsentStatus.GRANTED,
      )
  }

  fun onToggleAdStorage(enabled: Boolean) {
    val current = consentManager.getConsentPreferences()
    val updated =
      current.copy(
        adStorage = if (enabled) ConsentStatus.GRANTED else ConsentStatus.DENIED,
      )
    consentManager.setConsentPreferences(updated)
    loadConsentPreferences()
  }

  fun onToggleAdUserData(enabled: Boolean) {
    val current = consentManager.getConsentPreferences()
    val updated =
      current.copy(
        adUserData = if (enabled) ConsentStatus.GRANTED else ConsentStatus.DENIED,
      )
    consentManager.setConsentPreferences(updated)
    loadConsentPreferences()
  }

  fun onToggleAdPersonalization(enabled: Boolean) {
    val current = consentManager.getConsentPreferences()
    val updated =
      current.copy(
        adPersonalization = if (enabled) ConsentStatus.GRANTED else ConsentStatus.DENIED,
      )
    consentManager.setConsentPreferences(updated)
    loadConsentPreferences()
  }

  override fun navigate(navigation: Unit) {
    // Empty - this ViewModel doesn't navigate
  }
}

/**
 * UI state for Debug Settings screen
 */
data class DebugSettingsUiState(
  val isGenerating: Boolean = false,
  val isGeneratingPlayStore: Boolean = false,
  val adStorageEnabled: Boolean = false,
  val adUserDataEnabled: Boolean = false,
  val adPersonalizationEnabled: Boolean = false,
  val onPopulateFakeData: () -> Unit,
  val onPopulateFakePlayStoreData: () -> Unit,
  val onTriggerNotificationCheck: () -> Unit,
  val onToggleAdStorage: (Boolean) -> Unit,
  val onToggleAdUserData: (Boolean) -> Unit,
  val onToggleAdPersonalization: (Boolean) -> Unit,
)

/**
 * Side effects for Debug Settings screen
 */
sealed interface DebugSettingsSideEffect {
  data object FakeDataPopulated : DebugSettingsSideEffect

  data object FakePlayStoreDataPopulated : DebugSettingsSideEffect

  data object Error : DebugSettingsSideEffect
}
