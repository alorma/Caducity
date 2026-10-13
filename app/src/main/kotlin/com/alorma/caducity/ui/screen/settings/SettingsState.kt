package com.alorma.caducity.ui.screen.settings

import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.ThemeMode
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class CategoryMarginSummary(
  val name: String,
  /** Stored usual duration in days; `null` when the category has none. */
  val days: Int?,
)

data class SettingsState(
  val reminderEnabled: Boolean = false,
  val reminderTime: LocalTime = NotificationConfigDataSource.DEFAULT_TIME,
  val soonThresholdDays: Int = 3,
  /** Every category with its usual duration, shortest first (no duration last). */
  val categoryMargins: List<CategoryMarginSummary> = emptyList(),
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val colorPalette: ColorPalette = ColorPalette.REBOST,
  val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
)
