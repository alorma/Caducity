package com.alorma.caducity.ui.screen.calendar

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** The grid of one month: pure, so it can be tested without any UI. */
data class CalendarMonthModel(
  /** Day 1 of the month. */
  val first: LocalDate,
  val firstDayOfWeek: DayOfWeek,
) {
  init {
    require(first.day == 1) { "first must be day 1 of the month" }
  }

  val last: LocalDate get() = first.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))

  val daysInMonth: Int get() = last.day

  val range: ClosedRange<LocalDate> get() = first..last

  /** Weekday headers, starting at [firstDayOfWeek]. */
  val weekdays: List<DayOfWeek> get() = (0 until 7).map { DayOfWeek((firstDayOfWeek.isoDayNumber - 1 + it) % 7 + 1) }

  /** Empty cells before day 1. */
  val leadingBlanks: Int get() = (first.dayOfWeek.isoDayNumber - firstDayOfWeek.isoDayNumber + 7) % 7

  /** Rows of 7 cells; `null` is an invisible cell before day 1 or after the last day. */
  val weeks: List<List<LocalDate?>>
    get() {
      val cells =
        List(leadingBlanks) { null } + (0 until daysInMonth).map { first.plus(it, kotlinx.datetime.DateTimeUnit.DAY) }
      return cells.chunked(7).map { week -> week + List(7 - week.size) { null } }
    }

  fun previous(): CalendarMonthModel = copy(first = first.minus(DatePeriod(months = 1)))

  fun next(): CalendarMonthModel = copy(first = first.plus(DatePeriod(months = 1)))

  operator fun contains(date: LocalDate): Boolean = date in range

  companion object {
    fun of(
      date: LocalDate,
      firstDayOfWeek: DayOfWeek,
    ) = CalendarMonthModel(LocalDate(date.year, date.month, 1), firstDayOfWeek)
  }
}
