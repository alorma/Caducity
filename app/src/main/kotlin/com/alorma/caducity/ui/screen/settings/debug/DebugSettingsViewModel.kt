package com.alorma.caducity.ui.screen.settings.debug

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.GetActiveItemUseCase
import com.alorma.caducity.domain.usecase.PopulateFakeDataUseCase
import com.alorma.caducity.domain.usecase.fakedata.FakeDataStrategy
import com.alorma.caducity.domain.usecase.fakedata.FakeShowcaseDataStrategy
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
  private val fakeShowcaseDataStrategy: FakeShowcaseDataStrategy,
  private val consentManager: ConsentManager,
  private val getActiveItem: GetActiveItemUseCase,
) : BaseViewModel<Unit, DebugSettingsSideEffect, DebugSettingsSideEffect>() {
  private val _uiState =
    MutableStateFlow(
      DebugSettingsUiState(
        onPopulateFakeData = ::onPopulateFakeData,
        onPopulateShowcaseData = ::onPopulateShowcaseData,
        onOpenTransform = ::onOpenTransform,
        onOpenTransformFree = ::onOpenTransformFree,
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

  fun onPopulateFakeData() = populate(fakeTestDataStrategy)

  /** The pantry of the Play screenshots (spec §12). */
  fun onPopulateShowcaseData() = populate(fakeShowcaseDataStrategy)

  private fun populate(strategy: FakeDataStrategy) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isGenerating = true)

      val result = populateFakeDataUseCase.execute(strategy)

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

  /** Opens Transforma with the soonest unit of up to three products, until the real entries exist. */
  fun onOpenTransform() {
    viewModelScope.launch {
      val ids =
        getActiveItem
          .all()
          .filterNot { it.frozen }
          .sortedBy { it.expiration }
          .distinctBy { it.productId ?: it.itemId }
          .take(3)
          .map { it.itemId }
      emitSideEffect(if (ids.isEmpty()) DebugSettingsSideEffect.Error else DebugSettingsSideEffect.OpenTransform(ids))
    }
  }

  /** Opens Transforma in free entry (no ingredients), like the long press on the + will. */
  fun onOpenTransformFree() {
    emitSideEffect(DebugSettingsSideEffect.OpenTransform(emptyList()))
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
  val adStorageEnabled: Boolean = false,
  val adUserDataEnabled: Boolean = false,
  val adPersonalizationEnabled: Boolean = false,
  val onPopulateFakeData: () -> Unit,
  val onPopulateShowcaseData: () -> Unit,
  val onOpenTransform: () -> Unit,
  val onOpenTransformFree: () -> Unit,
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

  data object Error : DebugSettingsSideEffect

  data class OpenTransform(
    val itemIds: List<String>,
  ) : DebugSettingsSideEffect
}
