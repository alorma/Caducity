package com.alorma.caducity.feature.notification.worker

import com.alorma.caducity.config.clock.AppClock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class NotificationDelayCalculatorTest {
  private val utc = TimeZone.UTC

  @Test
  fun `returns delay until target time later today when target is in the future`() {
    // Given: now is 10:00 UTC, target is 12:00 UTC → delay should be 2 hours
    val appClock: AppClock =
      mock {
        on { now() } doReturn Instant.parse("2024-02-10T10:00:00Z")
      }
    val calculator = NotificationDelayCalculator(appClock)

    // When
    val delay = calculator.calculate(LocalTime(12, 0), utc)

    // Then
    expectThat(delay).isEqualTo(2.hours)
  }

  @Test
  fun `returns delay until target time tomorrow when target is in the past today`() {
    // Given: now is 14:00 UTC, target is 12:00 UTC → target today has passed, delay should be 22 hours
    val appClock: AppClock =
      mock {
        on { now() } doReturn Instant.parse("2024-02-10T14:00:00Z")
      }
    val calculator = NotificationDelayCalculator(appClock)

    // When
    val delay = calculator.calculate(LocalTime(12, 0), utc)

    // Then
    expectThat(delay).isEqualTo(22.hours)
  }

  @Test
  fun `returns 24 hours delay when target time is exactly now`() {
    // Given: now is exactly 12:00 UTC, target is 12:00 UTC → target is not in the future, delay should be 24 hours
    val appClock: AppClock =
      mock {
        on { now() } doReturn Instant.parse("2024-02-10T12:00:00Z")
      }
    val calculator = NotificationDelayCalculator(appClock)

    // When
    val delay = calculator.calculate(LocalTime(12, 0), utc)

    // Then
    expectThat(delay).isEqualTo(24.hours)
  }

  @Test
  fun `returns delay until midnight when target is midnight and now is just past midnight`() {
    // Given: now is 00:01 UTC, target is 00:00 UTC → target today has passed, delay should be 23h59m
    val appClock: AppClock =
      mock {
        on { now() } doReturn Instant.parse("2024-02-10T00:01:00Z")
      }
    val calculator = NotificationDelayCalculator(appClock)

    // When
    val delay = calculator.calculate(LocalTime(0, 0), utc)

    // Then
    expectThat(delay).isEqualTo(23.hours + 59.minutes)
  }

  @Test
  fun `returns 1 minute delay when target is 1 minute away`() {
    // Given: now is 11:59 UTC, target is 12:00 UTC → delay should be 1 minute
    val appClock: AppClock =
      mock {
        on { now() } doReturn Instant.parse("2024-02-10T11:59:00Z")
      }
    val calculator = NotificationDelayCalculator(appClock)

    // When
    val delay = calculator.calculate(LocalTime(12, 0), utc)

    // Then
    expectThat(delay).isEqualTo(1.minutes)
  }
}
