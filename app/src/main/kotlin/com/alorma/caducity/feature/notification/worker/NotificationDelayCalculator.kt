package com.alorma.caducity.feature.notification.worker

import com.alorma.caducity.config.clock.AppClock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Calculates the initial delay until the next occurrence of a notification time.
 */
class NotificationDelayCalculator(
  private val appClock: AppClock,
) {

  /**
   * Returns the duration from now until the next occurrence of [targetTime] in [timezone].
   * If [targetTime] is still in the future today, returns the duration until then.
   * Otherwise, returns the duration until [targetTime] tomorrow.
   */
  fun calculate(
    targetTime: LocalTime,
    timezone: TimeZone = TimeZone.currentSystemDefault(),
  ): Duration {
    val now = appClock.now()
    val nowLocal = now.toLocalDateTime(timezone)

    val targetToday = LocalDateTime(nowLocal.date, targetTime)
    val targetTodayInstant = targetToday.toInstant(timezone)

    return if (targetTodayInstant > now) {
      targetTodayInstant - now
    } else {
      targetTodayInstant + 1.days - now
    }
  }
}
