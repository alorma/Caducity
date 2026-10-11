package com.alorma.caducity.ui.screen.settings

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.ui.theme.ThemeMode
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class SettingsState(
  val reminderEnabled: Boolean = false,
  val reminderTime: LocalTime = NotificationConfigDataSource.DEFAULT_TIME,
  val soonThresholdDays: Int = 3,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val dynamicColors: Boolean = false,
  val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
)
