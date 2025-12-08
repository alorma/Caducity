package com.alorma.caducity.time.clock

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface AppClock {
  /**
   * Returns the current instant in UTC
   */
  fun now(): Instant

  /**
   * Returns the current LocalDateTime in the system's default timezone
   */
  fun nowAsLocalDateTime(): LocalDateTime

  /**
   * Returns the current LocalDateTime in the specified timezone
   */
  fun nowAsLocalDateTime(timeZone: TimeZone): LocalDateTime

  /**
   * Returns the current LocalDate in the system's default timezone
   */
  fun nowAsLocalDate(): LocalDate

  /**
   * Returns the current LocalDate in the specified timezone
   */
  fun nowAsLocalDate(timeZone: TimeZone): LocalDate

  /**
   * Returns the current timestamp in milliseconds since epoch
   */
  fun currentTimeMillis(): Long
}
