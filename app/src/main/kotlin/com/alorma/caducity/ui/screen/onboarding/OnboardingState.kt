package com.alorma.caducity.ui.screen.onboarding

import com.alorma.caducity.feature.consent.ConsentPreferences

data class OnboardingState(
  val currentPage: Int = 0,
  val isDisclaimerAccepted: Boolean = false,
  val isCompleted: Boolean = false,
  val consentPreferences: ConsentPreferences = ConsentPreferences.DEFAULT,
) {
  val totalPages: Int = 6 // Pages: 0=Welcome, 1=Features, 2=Permissions, 3=Tutorial, 4=Consent, 5=Disclaimer
  val isFirstPage: Boolean get() = currentPage == 0
  val isLastPage: Boolean get() = currentPage == totalPages - 1
  val canSkip: Boolean get() = currentPage < 4 // Can skip Welcome, Features, Permissions, Tutorial but not Consent or Disclaimer
}
