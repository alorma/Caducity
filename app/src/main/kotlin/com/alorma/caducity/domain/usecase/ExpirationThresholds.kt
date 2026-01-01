package com.alorma.caducity.domain.usecase

import kotlin.time.Duration

interface ExpirationThresholds {
  val soonExpiringThreshold: Duration
}