package com.alorma.caducity.ui.screen.settings

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.ui.theme.ThemeMode
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class CategoryMarginSummary(
  val name: String,
  val days: Int,
)

data class SettingsState(
  val reminderEnabled: Boolean = false,
  val reminderTime: LocalTime = NotificationConfigDataSource.DEFAULT_TIME,
  val soonThresholdDays: Int = 3,
  /** Every category with the margin that applies to it, shortest first. */
  val categoryMargins: List<CategoryMarginSummary> = emptyList(),
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val dynamicColors: Boolean = false,
  val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
)
