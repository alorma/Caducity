package com.alorma.caducity.domain.model

import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ExpiryStatusTest {
  private val today = LocalDate(2026, 10, 11)

  private fun status(
    daysAhead: Int,
    threshold: Int = 3,
    frozen: Boolean = false,
  ) = ExpiryStatus.of(today.plus(daysAhead, DateTimeUnit.DAY), today, threshold, frozen)

  @Test
  fun `yesterday is expired`() {
    expectThat(status(-1)).isEqualTo(ExpiryStatus.EXPIRED)
  }

  @Test
  fun `today is soon`() {
    expectThat(status(0)).isEqualTo(ExpiryStatus.SOON)
  }

  @Test
  fun `tomorrow is soon`() {
    expectThat(status(1)).isEqualTo(ExpiryStatus.SOON)
  }

  @Test
  fun `threshold day is soon and the next one is fresh`() {
    expectThat(status(3)).isEqualTo(ExpiryStatus.SOON)
    expectThat(status(4)).isEqualTo(ExpiryStatus.FRESH)
  }

  @Test
  fun `thresholds 1, 3 and 7`() {
    listOf(1, 3, 7).forEach { t ->
      expectThat(status(t, t)).isEqualTo(ExpiryStatus.SOON)
      expectThat(status(t + 1, t)).isEqualTo(ExpiryStatus.FRESH)
    }
  }

  @Test
  fun `frozen wins over any date`() {
    listOf(-10, 0, 2, 30).forEach { expectThat(status(it, frozen = true)).isEqualTo(ExpiryStatus.FROZEN) }
  }

  @Test
  fun `day rollover moves fresh to soon and soon to expired`() {
    val expiry = today.plus(4, DateTimeUnit.DAY)
    expectThat(ExpiryStatus.of(expiry, today, 3, false)).isEqualTo(ExpiryStatus.FRESH)
    expectThat(ExpiryStatus.of(expiry, today.plus(1, DateTimeUnit.DAY), 3, false)).isEqualTo(ExpiryStatus.SOON)
    expectThat(ExpiryStatus.of(expiry, today.plus(5, DateTimeUnit.DAY), 3, false)).isEqualTo(ExpiryStatus.EXPIRED)
  }

  /** Threshold 3 with `<=` is equivalent to the old calculation (`< (now + 4 days).date`) for 365 dates. */
  @Test
  fun `is equivalent to the old calculation for 365 dates`() {
    val utc = TimeZone.UTC
    val now = Instant.parse("2026-10-11T12:00:00Z")
    val nowDate = now.toLocalDateTime(utc).date
    val oldSoonLimit = (now + 4.days).toLocalDateTime(utc).date
    (-182..182).forEach { offset ->
      val expiry = nowDate.plus(offset, DateTimeUnit.DAY)
      val old =
        when {
          expiry < nowDate -> ExpiryStatus.EXPIRED
          expiry < oldSoonLimit -> ExpiryStatus.SOON
          else -> ExpiryStatus.FRESH
        }
      expectThat(ExpiryStatus.of(expiry, nowDate, 3, false)).isEqualTo(old)
    }
  }
}
