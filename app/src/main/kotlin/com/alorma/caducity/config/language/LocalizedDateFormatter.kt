package com.alorma.caducity.config.language

import com.alorma.caducity.ui.screen.dashboard.DaysOfWeekNames
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import java.time.format.TextStyle

class LocalizedDateFormatter(
  private val languageManager: LanguageManager,
) {
  fun getMonthName(month: Month): String {
    val locale = languageManager.getLocale()
    val javaMonth = java.time.Month.of(month.ordinal + 1)
    return javaMonth.getDisplayName(
      TextStyle.FULL_STANDALONE,
      locale,
    )
  }

  fun getDaysOfWeekNames(): DaysOfWeekNames {
    val locale = languageManager.getLocale()

    fun dayOfWeekName(javaDayOfWeek: java.time.DayOfWeek): String {
      return javaDayOfWeek.getDisplayName(
        TextStyle.SHORT,
        locale
      )
    }

    return DaysOfWeekNames(
      monday = dayOfWeekName(java.time.DayOfWeek.MONDAY),
      tuesday = dayOfWeekName(java.time.DayOfWeek.TUESDAY),
      wednesday = dayOfWeekName(java.time.DayOfWeek.WEDNESDAY),
      thursday = dayOfWeekName(java.time.DayOfWeek.THURSDAY),
      friday = dayOfWeekName(java.time.DayOfWeek.FRIDAY),
      saturday = dayOfWeekName(java.time.DayOfWeek.SATURDAY),
      sunday = dayOfWeekName(java.time.DayOfWeek.SUNDAY),
    )
  }
}