package com.alorma.caducity.ui.screen.onboarding

import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.model.SuggestedCategory
import kotlinx.datetime.LocalTime

enum class OnboardingStep {
  Welcome,
  Categories,
  Alerts,

  /** Informative; nothing is stored. */
  Transform,
  Privacy,
  Disclaimer,
}

data class OnboardingState(
  val step: OnboardingStep = OnboardingStep.Welcome,
  /** True when the user came back only to accept a new version of the legal notice (P3). */
  val reacceptOnly: Boolean = false,
  /** True for users who already used the app before 1.0.0: welcome, privacy and notice only, nothing else is stored. */
  val returning: Boolean = false,
  val goingForward: Boolean = true,
  val selectedCategories: Set<SuggestedCategory> =
    SuggestedCategory.entries.filter { it.selectedByDefault }.toSet(),
  val thresholdDays: Int = SoonThresholdPreferences.DEFAULT_DAYS,
  val reminderTime: LocalTime = NotificationConfigDataSource.DEFAULT_TIME,
  val remindersEnabled: Boolean = false,
  val analyticsEnabled: Boolean = false,
  val disclaimerChecked: Boolean = false,
  val completing: Boolean = false,
) {
  val steps: List<OnboardingStep>
    get() =
      if (returning) {
        listOf(OnboardingStep.Welcome, OnboardingStep.Privacy, OnboardingStep.Disclaimer)
      } else {
        OnboardingStep.entries
      }

  /** Steps that show «Pas N de M»: every one but the welcome. */
  val stepCount: Int get() = steps.size - 1

  /** 1..[stepCount] for the steps that show «Pas N de M»; null for the welcome, the re-acceptance and the returning users. */
  val stepNumber: Int?
    get() = if (reacceptOnly || returning) null else steps.indexOf(step).takeIf { it >= 1 }

  val canGoBack: Boolean get() = !reacceptOnly && step != OnboardingStep.Welcome
}
