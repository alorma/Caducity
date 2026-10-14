package com.alorma.caducity.ui.components.weekstrip

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.usecase.WeekStripData
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class WeekStripTest {
  private val today = LocalDate(2026, 10, 11)

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  @Test
  fun `first window has 7 days starting today`() {
    val days = WeekStripData(today, emptyMap()).window(0)

    expectThat(days.map { it.date }).hasSize(7)
    expectThat(days.first().date).isEqualTo(today)
    expectThat(days.last().date).isEqualTo(inDays(6))
  }

  @Test
  fun `swiping gives the following 7 days`() {
    val days = WeekStripData(today, emptyMap()).window(1)

    expectThat(days.first().date).isEqualTo(inDays(7))
    expectThat(days.last().date).isEqualTo(inDays(13))
  }

  @Test
  fun `at most 3 dots per day but the product count is kept`() {
    val data = WeekStripData(today, mapOf(today to List(5) { ExpiryStatus.SOON }))

    val day = data.window(0).first()

    expectThat(day.dots).hasSize(3)
    expectThat(day.productCount).isEqualTo(5)
  }

  @Test
  fun `expired never shows a dot`() {
    val data = WeekStripData(today, mapOf(today to listOf(ExpiryStatus.EXPIRED, ExpiryStatus.FRESH)))

    expectThat(data.window(0).first().dots).containsExactly(ExpiryStatus.FRESH)
  }

  @Test
  fun `empty days have no dots`() {
    expectThat(WeekStripData(today, emptyMap()).window(0).flatMap { it.dots }).isEmpty()
  }
}
