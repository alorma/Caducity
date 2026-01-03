package com.alorma.caducity.ui.screen.onboarding

data class OnboardingState(
  val currentPage: Int = 0,
  val isDisclaimerAccepted: Boolean = false,
  val isCompleted: Boolean = false,
) {
  val totalPages: Int = 5
  val isFirstPage: Boolean get() = currentPage == 0
  val isLastPage: Boolean get() = currentPage == totalPages - 1
  val canSkip: Boolean get() = currentPage < 4 // Can skip Welcome, Features, Permissions, Tutorial but not Disclaimer
}
