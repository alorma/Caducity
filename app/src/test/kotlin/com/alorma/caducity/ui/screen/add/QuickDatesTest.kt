package com.alorma.caducity.ui.screen.add

import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class QuickDatesTest {
  private val today = LocalDate(2026, 10, 11)

  @Test
  fun `quick dates count from today`() {
    expectThat(QuickDate.Tomorrow.from(today)).isEqualTo(LocalDate(2026, 10, 12))
    expectThat(QuickDate.ThreeDays.from(today)).isEqualTo(LocalDate(2026, 10, 14))
    expectThat(QuickDate.OneWeek.from(today)).isEqualTo(LocalDate(2026, 10, 18))
    expectThat(QuickDate.TwoWeeks.from(today)).isEqualTo(LocalDate(2026, 10, 25))
    expectThat(QuickDate.OneMonth.from(today)).isEqualTo(LocalDate(2026, 11, 11))
    expectThat(QuickDate.ThreeMonths.from(today)).isEqualTo(LocalDate(2027, 1, 11))
  }

  @Test
  fun `one month clamps to the end of a shorter month`() {
    expectThat(QuickDate.OneMonth.from(LocalDate(2026, 1, 31))).isEqualTo(LocalDate(2026, 2, 28))
  }

  @Test
  fun `crosses year boundary`() {
    expectThat(QuickDate.ThreeDays.from(LocalDate(2026, 12, 30))).isEqualTo(LocalDate(2027, 1, 2))
  }
}
