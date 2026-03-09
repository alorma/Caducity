package com.alorma.caducity.config.clock

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalTime::class)
interface AppClock {
  fun now(): Instant

  fun nowDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate
}
