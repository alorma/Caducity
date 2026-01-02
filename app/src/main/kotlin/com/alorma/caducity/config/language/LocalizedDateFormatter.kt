package com.alorma.caducity.config.language

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

  fun getDayOfWeekAbbreviation(dayOfWeek: DayOfWeek): String {
    val locale = languageManager.getLocale()
    // Convert kotlinx.datetime.DayOfWeek to java.time.DayOfWeek
    // kotlinx uses MONDAY=1...SUNDAY=7, java.time uses MONDAY=1...SUNDAY=7
    val javaDayOfWeek = java.time.DayOfWeek.of(dayOfWeek.ordinal + 1)
    return javaDayOfWeek.getDisplayName(
      TextStyle.SHORT,
      locale
    )
  }
}