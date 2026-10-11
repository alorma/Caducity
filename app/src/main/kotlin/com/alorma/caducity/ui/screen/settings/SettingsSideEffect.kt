package com.alorma.caducity.ui.screen.settings

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

sealed interface SettingsSideEffect {
  data class ShowTimePicker(
    val current: LocalTime,
  ) : SettingsSideEffect

  data class ShowWeekStartPicker(
    val current: DayOfWeek,
  ) : SettingsSideEffect

  /** The user refused the notification permission, so the reminder stays off. */
  data object NotificationsDenied : SettingsSideEffect
}
