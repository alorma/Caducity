package com.alorma.caducity.ui.screen.onboarding

import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.model.SuggestedCategory
import kotlinx.datetime.LocalTime

enum class OnboardingStep {
  Welcome,
  Categories,
  Alerts,
  Privacy,
  Disclaimer,
}

data class OnboardingState(
  val step: OnboardingStep = OnboardingStep.Welcome,
  /** True when the user came back only to accept a new version of the legal notice (P3). */
  val reacceptOnly: Boolean = false,
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
  /** 1..4 for the steps that show «Pas N de 4»; null for the welcome and the re-acceptance. */
  val stepNumber: Int?
    get() = if (reacceptOnly) null else step.ordinal.takeIf { it in 1..STEP_COUNT }

  val canGoBack: Boolean get() = !reacceptOnly && step != OnboardingStep.Welcome

  companion object {
    const val STEP_COUNT = 4
  }
}
