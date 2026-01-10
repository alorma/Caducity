package com.alorma.caducity.config.clock

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface AppClock {
  fun now(): Instant
  fun nowDate(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
  ): LocalDate
}
