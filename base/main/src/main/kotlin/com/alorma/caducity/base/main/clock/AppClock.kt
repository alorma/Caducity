package com.alorma.caducity.base.main.clock

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface AppClock {
  fun now(): Instant
}
