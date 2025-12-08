package com.alorma.caducity.time.clock

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class AppClock(private val clock: Clock = Clock.System) {

  /**
   * Returns the current instant in UTC
   */
  fun now(): Instant = clock.now()

  /**
   * Returns the current LocalDateTime in the system's default timezone
   */
  fun nowAsLocalDateTime(): LocalDateTime =
    clock.now().toLocalDateTime(TimeZone.currentSystemDefault())

  /**
   * Returns the current LocalDateTime in the specified timezone
   */
  fun nowAsLocalDateTime(timeZone: TimeZone): LocalDateTime =
    clock.now().toLocalDateTime(timeZone)

  /**
   * Returns the current LocalDate in the system's default timezone
   */
  fun nowAsLocalDate(): LocalDate =
    nowAsLocalDateTime().date

  /**
   * Returns the current LocalDate in the specified timezone
   */
  fun nowAsLocalDate(timeZone: TimeZone): LocalDate =
    nowAsLocalDateTime(timeZone).date

  /**
   * Returns the current timestamp in milliseconds since epoch
   */
  fun currentTimeMillis(): Long = clock.now().toEpochMilliseconds()
}
