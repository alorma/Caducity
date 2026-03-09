package com.alorma.caducity.config.time

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.date(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = this.toLocalDateTime(timeZone).date
