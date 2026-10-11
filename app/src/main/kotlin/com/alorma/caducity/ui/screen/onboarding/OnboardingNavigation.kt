package com.alorma.caducity.ui.screen.onboarding

import kotlinx.datetime.LocalTime

sealed interface OnboardingNavigation {
  data object CompleteOnboarding : OnboardingNavigation
}

sealed interface OnboardingNavigationSideEffect {
  data object NavigateToApp : OnboardingNavigationSideEffect
}

sealed interface OnboardingSideEffect {
  data class ShowTimePicker(
    val current: LocalTime,
  ) : OnboardingSideEffect
}
