package com.alorma.caducity.feature.notification

import java.util.Locale
import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ReminderDayLabelTest {
  private val ca = Locale.forLanguageTag("ca")
  private val en = Locale.ENGLISH

  @Test
  fun `weekday name inside six days`() {
    // 2026-10-15 is a Thursday
    expectThat(ReminderDayLabel.format(LocalDate(2026, 10, 15), daysLeft = 4, locale = ca)).isEqualTo("dijous")
    expectThat(ReminderDayLabel.format(LocalDate(2026, 10, 15), daysLeft = 4, locale = en)).isEqualTo("thursday")
    expectThat(ReminderDayLabel.format(LocalDate(2026, 10, 17), daysLeft = 6, locale = en)).isEqualTo("saturday")
  }

  @Test
  fun `short weekday and day of month from seven days`() {
    // 2026-10-26 is a Monday
    expectThat(ReminderDayLabel.format(LocalDate(2026, 10, 26), daysLeft = 15, locale = ca)).isEqualTo("dl 26")
    expectThat(ReminderDayLabel.format(LocalDate(2026, 10, 26), daysLeft = 7, locale = en)).isEqualTo("mon 26")
  }
}
