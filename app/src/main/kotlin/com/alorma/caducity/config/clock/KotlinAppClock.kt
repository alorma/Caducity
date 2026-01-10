package com.alorma.caducity.config.clock

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class KotlinAppClock(private val clock: Clock = Clock.System) : AppClock {
  override fun now(): Instant = clock.now()

  override fun nowDate(timeZone: TimeZone): LocalDate = now()
    .toLocalDateTime(timeZone)
    .date
}
