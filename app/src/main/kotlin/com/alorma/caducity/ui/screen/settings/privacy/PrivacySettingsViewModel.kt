package com.alorma.caducity.ui.screen.settings.privacy

import androidx.lifecycle.ViewModel
import com.alorma.caducity.feature.consent.ConsentManager
import com.alorma.caducity.feature.consent.ConsentPreferences
import com.alorma.caducity.feature.consent.ConsentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PrivacySettingsViewModel(
  private val consentManager: ConsentManager,
) : ViewModel() {

  private val _state = MutableStateFlow(PrivacySettingsState())
  val state: StateFlow<PrivacySettingsState> = _state.asStateFlow()

  init {
    loadCurrentConsent()
  }

  private fun loadCurrentConsent() {
    val preferences = consentManager.getConsentPreferences()
    _state.update {
      it.copy(analyticsEnabled = preferences.analyticsStorage == ConsentStatus.GRANTED)
    }
  }

  fun onAnalyticsToggle(enabled: Boolean) {
    val preferences = if (enabled) {
      ConsentPreferences.ANALYTICS_ONLY
    } else {
      ConsentPreferences.DEFAULT
    }

    consentManager.setConsentPreferences(preferences)

    _state.update {
      it.copy(analyticsEnabled = enabled)
    }
  }
}

data class PrivacySettingsState(
  val analyticsEnabled: Boolean = false,
)
