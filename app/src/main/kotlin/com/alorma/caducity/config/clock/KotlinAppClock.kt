package com.alorma.caducity.config.clock

import kotlin.time.Clock
import kotlin.time.Instant

class KotlinAppClock(private val clock: Clock = Clock.System) : AppClock {
  override fun now(): Instant = clock.now()
}
