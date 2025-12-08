package com.alorma.caducity.time.clock

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class KotlinAppClock(private val clock: Clock = Clock.System) : AppClock {

  override fun now(): Instant = clock.now()

  override fun nowAsLocalDateTime(): LocalDateTime =
    clock.now().toLocalDateTime(TimeZone.currentSystemDefault())

  override fun nowAsLocalDateTime(timeZone: TimeZone): LocalDateTime =
    clock.now().toLocalDateTime(timeZone)

  override fun nowAsLocalDate(): LocalDate =
    nowAsLocalDateTime().date

  override fun nowAsLocalDate(timeZone: TimeZone): LocalDate =
    nowAsLocalDateTime(timeZone).date

  override fun currentTimeMillis(): Long = clock.now().toEpochMilliseconds()
}
