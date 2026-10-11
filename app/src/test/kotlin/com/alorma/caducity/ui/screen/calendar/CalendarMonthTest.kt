package com.alorma.caducity.ui.screen.calendar

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class CalendarMonthTest {
  private fun october(firstDayOfWeek: DayOfWeek) = CalendarMonthModel.of(LocalDate(2026, 10, 21), firstDayOfWeek)

  @Test
  fun `october 2026 starts on thursday`() {
    // 1 Oct 2026 is a Thursday.
    expectThat(october(DayOfWeek.MONDAY).leadingBlanks).isEqualTo(3)
    expectThat(october(DayOfWeek.SUNDAY).leadingBlanks).isEqualTo(4)
    expectThat(october(DayOfWeek.SATURDAY).leadingBlanks).isEqualTo(5)
  }

  @Test
  fun `weekday headers start at the chosen first day`() {
    expectThat(october(DayOfWeek.MONDAY).weekdays.first()).isEqualTo(DayOfWeek.MONDAY)
    expectThat(october(DayOfWeek.SATURDAY).weekdays).containsExactly(
      DayOfWeek.SATURDAY,
      DayOfWeek.SUNDAY,
      DayOfWeek.MONDAY,
      DayOfWeek.TUESDAY,
      DayOfWeek.WEDNESDAY,
      DayOfWeek.THURSDAY,
      DayOfWeek.FRIDAY,
    )
  }

  @Test
  fun `months have 28 to 31 days and leap years are respected`() {
    fun days(
      year: Int,
      month: Int,
    ) = CalendarMonthModel.of(LocalDate(year, month, 5), DayOfWeek.MONDAY).daysInMonth

    expectThat(days(2026, 2)).isEqualTo(28)
    expectThat(days(2028, 2)).isEqualTo(29)
    expectThat(days(2026, 4)).isEqualTo(30)
    expectThat(days(2026, 10)).isEqualTo(31)
    expectThat(days(2100, 2)).isEqualTo(28)
    expectThat(days(2000, 2)).isEqualTo(29)
  }

  @Test
  fun `grid is made of full weeks with invisible cells around the month`() {
    val weeks = october(DayOfWeek.MONDAY).weeks

    expectThat(weeks.all { it.size == 7 }).isEqualTo(true)
    expectThat(weeks.first().take(3)).containsExactly(null, null, null)
    expectThat(weeks.first()[3]).isEqualTo(LocalDate(2026, 10, 1))
    expectThat(weeks.last().last()).isNull()
    expectThat(weeks.flatten().filterNotNull()).hasSize(31)
  }

  @Test
  fun `a month that fits in four weeks has four rows`() {
    // Feb 2027 starts on a Monday and has 28 days.
    val weeks = CalendarMonthModel.of(LocalDate(2027, 2, 10), DayOfWeek.MONDAY).weeks
    expectThat(weeks.size).isEqualTo(4)
  }

  @Test
  fun `previous and next wrap the year`() {
    val december = CalendarMonthModel.of(LocalDate(2026, 12, 3), DayOfWeek.MONDAY)
    expectThat(december.next().first).isEqualTo(LocalDate(2027, 1, 1))
    expectThat(CalendarMonthModel.of(LocalDate(2027, 1, 20), DayOfWeek.MONDAY).previous().first)
      .isEqualTo(LocalDate(2026, 12, 1))
  }

  @Test
  fun `range covers the whole month and contains works`() {
    val month = october(DayOfWeek.MONDAY)
    expectThat(month.range.start).isEqualTo(LocalDate(2026, 10, 1))
    expectThat(month.range.endInclusive).isEqualTo(LocalDate(2026, 10, 31))
    expectThat(LocalDate(2026, 10, 15) in month).isEqualTo(true)
    expectThat(LocalDate(2026, 11, 1) in month).isEqualTo(false)
  }
}
