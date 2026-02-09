package com.alorma.caducity.ui.screen.onboarding

import com.alorma.caducity.feature.consent.ConsentFlag
import com.alorma.caducity.feature.consent.ConsentManager
import com.alorma.caducity.feature.consent.ConsentPreferences
import com.alorma.caducity.feature.tracking.CompleteOnboardingAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnboardingViewModel(
  private val onboardingFlag: OnboardingFlag,
  private val consentFlag: ConsentFlag,
  private val consentManager: ConsentManager,
  private val eventTracker: EventTracker,
) : BaseViewModel<OnboardingNavigation, OnboardingNavigationSideEffect, Unit>() {

  private val _state = MutableStateFlow(OnboardingState())
  val state: StateFlow<OnboardingState> = _state.asStateFlow()

  fun nextPage() {
    _state.update { currentState ->
      if (currentState.currentPage < currentState.totalPages - 1) {
        currentState.copy(currentPage = currentState.currentPage + 1)
      } else {
        currentState
      }
    }
  }

  fun previousPage() {
    _state.update { currentState ->
      if (currentState.currentPage > 0) {
        currentState.copy(currentPage = currentState.currentPage - 1)
      } else {
        currentState
      }
    }
  }

  fun goToPage(page: Int) {
    _state.update { it.copy(currentPage = page) }
  }

  fun skipOnboarding() {
    if (_state.value.canSkip) {
      completeOnboarding()
    }
  }

  fun acceptDisclaimer() {
    _state.update { it.copy(isDisclaimerAccepted = true) }
    completeOnboarding()
  }

  fun setConsentPreferences(preferences: ConsentPreferences) {
    consentManager.setConsentPreferences(preferences)
    _state.update { it.copy(consentPreferences = preferences) }
  }

  private fun completeOnboarding() {
    // Disable consent flag when user completes onboarding
    consentFlag.disable()
    onboardingFlag.disable()
    _state.update { it.copy(isCompleted = true) }
    navigate(OnboardingNavigation.CompleteOnboarding)
  }

  override fun navigate(navigation: OnboardingNavigation) {
    when (navigation) {
      OnboardingNavigation.CompleteOnboarding -> {
        eventTracker.trackAction(CompleteOnboardingAction())
        emitNavigationSideEffect(OnboardingNavigationSideEffect.NavigateToApp)
      }
    }
  }
}
