package com.alorma.caducity.ui.screen.onboarding

sealed interface OnboardingNavigation {
  data object CompleteOnboarding : OnboardingNavigation
}

sealed interface OnboardingNavigationSideEffect {
  data object NavigateToApp : OnboardingNavigationSideEffect
}
