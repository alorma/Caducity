package com.alorma.caducity.domain.usecase

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** Expiration dates are `LocalDate`s in the domain, stored as local midnight. */
internal fun LocalDate.toExpirationInstant(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant =
  atStartOfDayIn(timeZone)
