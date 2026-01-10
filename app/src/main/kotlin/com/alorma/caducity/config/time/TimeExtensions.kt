package com.alorma.caducity.config.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.date(
  timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDate {
  return this.toLocalDateTime(timeZone).date
}