package com.alorma.caducity.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.feature.tracking.CompleteOnboardingAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.screen.onboarding.OnboardingFlag
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
  private val onboardingFlag: OnboardingFlag,
  private val eventTracker: EventTracker,
) : ViewModel() {

  private val _state = MutableStateFlow(OnboardingState())
  val state: StateFlow<OnboardingState> = _state.asStateFlow()

  private val navigationSideEffectChannel = Channel<OnboardingNavigationSideEffect>()
  val navigationSideEffects = navigationSideEffectChannel.receiveAsFlow()

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
    navigate(OnboardingNavigation.CompleteOnboarding)
  }

  fun navigate(navigation: OnboardingNavigation) {
    when (navigation) {
      OnboardingNavigation.CompleteOnboarding -> {
        eventTracker.trackAction(CompleteOnboardingAction())
        emitNavigationSideEffect(OnboardingNavigationSideEffect.NavigateToApp)
      }
    }
  }

  private fun emitNavigationSideEffect(effect: OnboardingNavigationSideEffect) {
    viewModelScope.launch {
      navigationSideEffectChannel.send(effect)
    }
  }
}
