package com.alorma.caducity.ui.screen.onboarding

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.DisclaimerPreferences
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.model.SeedCategory
import com.alorma.caducity.domain.model.SuggestedCategory
import com.alorma.caducity.domain.model.SuggestedThresholds
import com.alorma.caducity.domain.usecase.SeedCategoriesUseCase
import com.alorma.caducity.feature.consent.ConsentFlag
import com.alorma.caducity.feature.consent.ConsentManager
import com.alorma.caducity.feature.consent.ConsentPreferences
import com.alorma.caducity.feature.consent.ConsentStatus
import com.alorma.caducity.feature.notification.ExpirationWorkScheduler
import com.alorma.caducity.feature.tracking.CompleteOnboardingAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.screen.transform.TransformFeature
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

/**
 * Onboarding: welcome → categories → alerts → (Transforma, only with `transform_enabled`) → privacy → legal notice. Nothing is stored until the notice is accepted,
 * so a process death mid-way just restarts the flow. Users who already finished the old onboarding only see the
 * notice (re-acceptance) and nothing else is touched. Users who used the app before 1.0.0 get a short informative
 * version (welcome, privacy, notice): their categories, margin and reminder are kept, only the consent is asked again.
 */
class OnboardingViewModel(
  private val onboardingFlag: OnboardingFlag,
  private val legacyOnboardingFlag: LegacyOnboardingFlag,
  private val consentFlag: ConsentFlag,
  private val consentManager: ConsentManager,
  private val disclaimerPreferences: DisclaimerPreferences,
  private val soonThresholdPreferences: SoonThresholdPreferences,
  private val notificationConfig: NotificationConfigDataSource,
  private val workScheduler: ExpirationWorkScheduler,
  private val seedCategories: SeedCategoriesUseCase,
  private val stringProvider: StringProvider,
  private val appClock: AppClock,
  private val eventTracker: EventTracker,
  transformFeature: TransformFeature,
) : BaseViewModel<OnboardingNavigation, OnboardingNavigationSideEffect, OnboardingSideEffect>() {
  private val _state =
    MutableStateFlow(
      if (!onboardingFlag.isEnabled() && disclaimerPreferences.needsAcceptance()) {
        OnboardingState(step = OnboardingStep.Disclaimer, reacceptOnly = true)
      } else if (onboardingFlag.isEnabled() && !legacyOnboardingFlag.isEnabled()) {
        OnboardingState(
          returning = true,
          analyticsEnabled = consentManager.getConsentPreferences().analyticsStorage == ConsentStatus.GRANTED,
        )
      } else {
        OnboardingState(transformStep = transformFeature.isEnabled())
      },
    )
  val state: StateFlow<OnboardingState> = _state.asStateFlow()

  fun onContinue() {
    _state.update { current ->
      val next = current.steps.getOrNull(current.steps.indexOf(current.step) + 1)
      if (next == null || current.reacceptOnly) current else current.copy(step = next, goingForward = true)
    }
  }

  fun onBack() {
    _state.update { current ->
      val previous = current.steps.getOrNull(current.steps.indexOf(current.step) - 1)
      if (previous == null || !current.canGoBack) current else current.copy(step = previous, goingForward = false)
    }
  }

  fun onToggleCategory(category: SuggestedCategory) {
    _state.update {
      val selected = it.selectedCategories
      it.copy(selectedCategories = if (category in selected) selected - category else selected + category)
    }
  }

  fun onThresholdChange(days: Int) {
    if (days in SoonThresholdPreferences.ALLOWED_DAYS) _state.update { it.copy(thresholdDays = days) }
  }

  fun onReminderTimeClick() {
    emitSideEffect(OnboardingSideEffect.ShowTimePicker(_state.value.reminderTime))
  }

  fun onReminderTimeChange(time: LocalTime) {
    _state.update { it.copy(reminderTime = time) }
  }

  /** The UI has the notification permission (just granted or from before): the reminder is on and the flow moves on. */
  fun onRemindersGranted() {
    _state.update { it.copy(remindersEnabled = true) }
    onContinue()
  }

  /** The user said no to the permission, or tapped «Ara no»: the reminder stays off and the flow moves on. */
  fun onRemindersDeclined() {
    _state.update { it.copy(remindersEnabled = false) }
    onContinue()
  }

  fun onAnalyticsChange(enabled: Boolean) {
    _state.update { it.copy(analyticsEnabled = enabled) }
  }

  fun onDisclaimerCheckedChange(checked: Boolean) {
    _state.update { it.copy(disclaimerChecked = checked) }
  }

  fun onAccept() {
    val current = _state.value
    if (!current.disclaimerChecked || current.completing) return
    _state.update { it.copy(completing = true) }
    viewModelScope.launch {
      try {
        if (!current.reacceptOnly && !current.returning) saveChoices(current)
        if (current.returning) saveConsent(current)
        disclaimerPreferences.accept(appClock.now())
        if (!current.reacceptOnly) {
          consentFlag.disable()
          onboardingFlag.disable()
        }
        navigate(OnboardingNavigation.CompleteOnboarding)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(completing = false) }
        throw e
      }
    }
  }

  private suspend fun saveChoices(state: OnboardingState) {
    val seeds =
      SuggestedCategory.entries
        .filter { it in state.selectedCategories }
        .map { SeedCategory(stringProvider.getString(it.nameRes), it.shape, it.icon, SuggestedThresholds.of(it)) }
    seedCategories.seed(seeds).getOrThrow()

    soonThresholdPreferences.setThresholdDays(state.thresholdDays)
    notificationConfig.setNotificationTime(state.reminderTime)
    notificationConfig.setNotificationsEnabled(state.remindersEnabled)
    if (state.remindersEnabled) {
      workScheduler.scheduleExpirationCheck(state.reminderTime)
    } else {
      workScheduler.cancelExpirationCheck()
    }

    saveConsent(state)
  }

  private fun saveConsent(state: OnboardingState) {
    consentManager.setConsentPreferences(
      if (state.analyticsEnabled) ConsentPreferences.ANALYTICS_ONLY else ConsentPreferences.DEFAULT,
    )
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
