package com.alorma.caducity.ui.screen.onboarding

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.feature.notification.hasNotificationPermission
import com.alorma.caducity.feature.tracking.OnboardingScreen as OnboardingScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.motion.rememberReducedMotion
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.screen.onboarding.steps.AlertsStep
import com.alorma.caducity.ui.screen.onboarding.steps.CategoriesStep
import com.alorma.caducity.ui.screen.onboarding.steps.DisclaimerStep
import com.alorma.caducity.ui.screen.onboarding.steps.PrivacyStep
import com.alorma.caducity.ui.screen.onboarding.steps.WelcomeStep
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
  onComplete: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: OnboardingViewModel = koinViewModel(),
) {
  TrackScreen(screen = OnboardingScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val reducedMotion = rememberReducedMotion()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        OnboardingNavigationSideEffect.NavigateToApp -> onComplete()
      }
    }
  }
  SideEffectHandler(viewModel, dialogState)

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) viewModel.onRemindersGranted() else viewModel.onRemindersDeclined()
    }
  val hasPermission = context.hasNotificationPermission()

  BackHandler(enabled = state.canGoBack) { viewModel.onBack() }

  AppScaffold(modifier = modifier.fillMaxSize(), dialogState = dialogState) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      AnimatedContent(
        targetState = state.step,
        transitionSpec = { stepTransition(state.goingForward, reducedMotion) },
        label = "onboarding-step",
      ) { step ->
        when (step) {
          OnboardingStep.Welcome -> WelcomeStep(onStart = viewModel::onContinue)
          OnboardingStep.Categories ->
            CategoriesStep(
              stepNumber = 1,
              selected = state.selectedCategories,
              onToggle = viewModel::onToggleCategory,
              onBack = viewModel::onBack,
              onContinue = viewModel::onContinue,
            )
          OnboardingStep.Alerts ->
            AlertsStep(
              stepNumber = 2,
              thresholdDays = state.thresholdDays,
              time = state.reminderTime,
              permissionGranted = hasPermission,
              onThresholdChange = viewModel::onThresholdChange,
              onTimeClick = viewModel::onReminderTimeClick,
              onEnable = {
                if (hasPermission) {
                  viewModel.onRemindersGranted()
                } else {
                  permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
              },
              onNotNow = viewModel::onRemindersDeclined,
              onBack = viewModel::onBack,
            )
          OnboardingStep.Privacy ->
            PrivacyStep(
              stepNumber = state.stepNumber,
              analyticsEnabled = state.analyticsEnabled,
              onAnalyticsChange = viewModel::onAnalyticsChange,
              onBack = viewModel::onBack,
              onContinue = viewModel::onContinue,
            )
          OnboardingStep.Disclaimer ->
            DisclaimerStep(
              stepNumber = state.stepNumber,
              checked = state.disclaimerChecked,
              onCheckedChange = viewModel::onDisclaimerCheckedChange,
              onBack = if (state.canGoBack) viewModel::onBack else null,
              onAccept = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                viewModel.onAccept()
              },
              accepting = state.completing,
            )
        }
      }
    }
  }
}

private fun stepTransition(
  forward: Boolean,
  reducedMotion: Boolean,
): ContentTransform {
  if (reducedMotion) return EnterTransition.None togetherWith ExitTransition.None
  val sign = if (forward) 1 else -1
  return (slideInHorizontally { width -> sign * width / 4 } + fadeIn()) togetherWith
    (slideOutHorizontally { width -> -sign * width / 4 } + fadeOut())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SideEffectHandler(
  viewModel: OnboardingViewModel,
  dialogState: AppDialogState,
) {
  val scope = rememberCoroutineScope()
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is OnboardingSideEffect.ShowTimePicker ->
          scope.launch {
            val pickerState = TimePickerState(effect.current.hour, effect.current.minute, is24Hour = true)
            val result =
              dialogState.showAlertDialog(
                title = { Text(stringResource(R.string.settings_time_dialog_title)) },
                content = { TimePicker(state = pickerState) },
                positiveButton = { Text(stringResource(R.string.settings_dialog_save)) },
                negativeButton = { Text(stringResource(R.string.settings_dialog_cancel)) },
                type = AppFeedbackType.Info,
              )
            if (result == DialogResult.Positive) {
              viewModel.onReminderTimeChange(LocalTime(pickerState.hour, pickerState.minute))
            }
          }
      }
    }
  }
}
