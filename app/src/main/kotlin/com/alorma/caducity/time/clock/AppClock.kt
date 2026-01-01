package com.alorma.caducity.time.clock

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface AppClock {
  fun now(): Instant
}
