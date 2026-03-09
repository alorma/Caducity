package com.alorma.caducity.config.language

import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.Month
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames

class LocalizedDateFormatter {
  fun getMonthNames(): MonthNames =
    MonthNames(
      january = getMonthName(Month.JANUARY),
      february = getMonthName(Month.FEBRUARY),
      march = getMonthName(Month.MARCH),
      april = getMonthName(Month.APRIL),
      may = getMonthName(Month.MAY),
      june = getMonthName(Month.JUNE),
      july = getMonthName(Month.JULY),
      august = getMonthName(Month.AUGUST),
      september = getMonthName(Month.SEPTEMBER),
      october = getMonthName(Month.OCTOBER),
      november = getMonthName(Month.NOVEMBER),
      december = getMonthName(Month.DECEMBER),
    )

  private fun getMonthName(month: Month): String {
    val locale = Locale.getDefault()
    val javaMonth = java.time.Month.of(month.ordinal + 1)
    return javaMonth.getDisplayName(
      TextStyle.FULL_STANDALONE,
      locale,
    )
  }

  fun getDaysOfWeekNames(): DayOfWeekNames =
    DayOfWeekNames(
      monday = dayOfWeekName(java.time.DayOfWeek.MONDAY),
      tuesday = dayOfWeekName(java.time.DayOfWeek.TUESDAY),
      wednesday = dayOfWeekName(java.time.DayOfWeek.WEDNESDAY),
      thursday = dayOfWeekName(java.time.DayOfWeek.THURSDAY),
      friday = dayOfWeekName(java.time.DayOfWeek.FRIDAY),
      saturday = dayOfWeekName(java.time.DayOfWeek.SATURDAY),
      sunday = dayOfWeekName(java.time.DayOfWeek.SUNDAY),
    )

  private fun dayOfWeekName(javaDayOfWeek: java.time.DayOfWeek): String {
    val locale = Locale.getDefault()
    return javaDayOfWeek.getDisplayName(
      TextStyle.SHORT,
      locale,
    )
  }

  fun getDayOfWeekFullName(dayOfWeek: kotlinx.datetime.DayOfWeek): String {
    val locale = Locale.getDefault()
    val javaDayOfWeek = java.time.DayOfWeek.of(dayOfWeek.ordinal + 1)
    return javaDayOfWeek.getDisplayName(
      TextStyle.FULL_STANDALONE,
      locale,
    )
  }
}
