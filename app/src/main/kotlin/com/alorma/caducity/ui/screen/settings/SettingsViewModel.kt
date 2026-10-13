package com.alorma.caducity.ui.screen.settings

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.calendar.FirstDayOfWeekPreferences
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.feature.notification.ExpirationWorkScheduler
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateFromSettingsAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

/**
 * Ajustos: the daily reminder, the «Aviat» threshold, theme and week start. Every change is stored right away and the
 * daily worker is scheduled again when the switch, the time or the threshold change.
 */
class SettingsViewModel(
  private val notificationConfig: NotificationConfigDataSource,
  private val workScheduler: ExpirationWorkScheduler,
  private val soonThreshold: SoonThresholdPreferences,
  observeCategories: ObserveCategoriesUseCase,
  private val themePreferences: ThemePreferences,
  private val firstDayOfWeekPreferences: FirstDayOfWeekPreferences,
  private val eventTracker: EventTracker,
) : BaseViewModel<SettingsNavigation, SettingsNavigationSideEffect, SettingsSideEffect>() {
  private data class Reminder(
    val enabled: Boolean = false,
    val time: LocalTime = NotificationConfigDataSource.DEFAULT_TIME,
  )

  private val reminder = MutableStateFlow(Reminder())

  val state: StateFlow<SettingsState> =
    combine(
      reminder,
      combine(soonThreshold.thresholdDays, observeCategories.observe()) { days, categories ->
        days to categories
      },
      snapshotFlow { themePreferences.themeMode.value },
      snapshotFlow { themePreferences.colorPalette.value == ColorPalette.DYNAMIC },
      firstDayOfWeekPreferences.state,
    ) { reminder, (threshold, categories), themeMode, dynamicColors, calendar ->
      SettingsState(
        reminderEnabled = reminder.enabled,
        reminderTime = reminder.time,
        soonThresholdDays = threshold,
        categoryMargins =
          categories
            .map { CategoryMarginSummary(it.name, it.soonThresholdDays ?: threshold) }
            .sortedWith(compareBy({ it.days }, { it.name.normalizeForSearch() })),
        themeMode = themeMode,
        dynamicColors = dynamicColors,
        firstDayOfWeek = calendar.firstDayOfWeek,
      )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

  init {
    viewModelScope.launch {
      reminder.value =
        Reminder(
          enabled = notificationConfig.isNotificationsEnabled(),
          time = notificationConfig.getNotificationTime(),
        )
    }
  }

  override fun navigate(navigation: SettingsNavigation) {
    when (navigation) {
      SettingsNavigation.Backup -> {
        eventTracker.trackAction(NavigateFromSettingsAction("backup"))
        emitNavigationSideEffect(SettingsNavigationSideEffect.OpenBackup)
      }
      SettingsNavigation.CategoryMargins -> {
        eventTracker.trackAction(NavigateFromSettingsAction("category_margins"))
        emitNavigationSideEffect(SettingsNavigationSideEffect.OpenCategoryMargins)
      }
      SettingsNavigation.Privacy -> {
        eventTracker.trackAction(NavigateFromSettingsAction("privacy"))
        emitNavigationSideEffect(SettingsNavigationSideEffect.OpenPrivacy)
      }
      SettingsNavigation.About -> {
        eventTracker.trackAction(NavigateFromSettingsAction("about"))
        emitNavigationSideEffect(SettingsNavigationSideEffect.OpenAbout)
      }
      SettingsNavigation.Debug -> {
        eventTracker.trackAction(NavigateFromSettingsAction("debug"))
        emitNavigationSideEffect(SettingsNavigationSideEffect.OpenDebug)
      }
    }
  }

  /** The UI has already made sure the notification permission is granted before turning the reminder on. */
  fun onReminderToggle(enabled: Boolean) {
    viewModelScope.launch {
      notificationConfig.setNotificationsEnabled(enabled)
      reminder.update { it.copy(enabled = enabled) }
      if (enabled) {
        workScheduler.scheduleExpirationCheck(reminder.value.time)
      } else {
        workScheduler.cancelExpirationCheck()
      }
    }
  }

  fun onNotificationsDenied() {
    emitSideEffect(SettingsSideEffect.NotificationsDenied)
  }

  fun onReminderTimeClick() {
    emitSideEffect(SettingsSideEffect.ShowTimePicker(reminder.value.time))
  }

  fun onReminderTimeChange(time: LocalTime) {
    viewModelScope.launch {
      notificationConfig.setNotificationTime(time)
      reminder.update { it.copy(time = time) }
      rescheduleIfEnabled()
    }
  }

  fun onSoonThresholdChange(days: Int) {
    soonThreshold.setThresholdDays(days)
    viewModelScope.launch { rescheduleIfEnabled() }
  }

  fun onThemeModeChange(mode: ThemeMode) {
    themePreferences.setThemeModeState(mode)
  }

  fun onDynamicColorsChange(enabled: Boolean) {
    themePreferences.setColorPalette(if (enabled) ColorPalette.DYNAMIC else ColorPalette.REBOST)
  }

  fun onWeekStartClick() {
    emitSideEffect(SettingsSideEffect.ShowWeekStartPicker(firstDayOfWeekPreferences.state.value.firstDayOfWeek))
  }

  fun onFirstDayOfWeekChange(day: DayOfWeek) {
    firstDayOfWeekPreferences.setFirstDayOfWeek(day)
  }

  private fun rescheduleIfEnabled() {
    val current = reminder.value
    if (current.enabled) workScheduler.rescheduleExpirationCheck(current.time)
  }
}
