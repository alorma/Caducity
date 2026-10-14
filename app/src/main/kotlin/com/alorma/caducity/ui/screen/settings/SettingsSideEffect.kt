package com.alorma.caducity.ui.screen.settings

import kotlinx.datetime.LocalTime

sealed interface SettingsSideEffect {
  data class ShowTimePicker(
    val current: LocalTime,
  ) : SettingsSideEffect

  /** «Colors» as a modal bottom sheet over Settings. */
  data object ShowColors : SettingsSideEffect

  /** The user refused the notification permission, so the reminder stays off. */
  data object NotificationsDenied : SettingsSideEffect
}
