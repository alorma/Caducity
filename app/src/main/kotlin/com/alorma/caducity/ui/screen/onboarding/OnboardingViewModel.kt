package com.alorma.caducity.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import com.alorma.caducity.onboarding.OnboardingFlag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnboardingViewModel(
  private val onboardingFlag: OnboardingFlag,
) : ViewModel() {

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

  private fun completeOnboarding() {
    onboardingFlag.disable()
    _state.update { it.copy(isCompleted = true) }
  }
}
