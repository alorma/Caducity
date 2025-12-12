package com.alorma.caducity.domain.usecase

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class AppExpirationThresholdsImpl: ExpirationThresholds {
  override val soonExpiringThreshold: Duration = 7.days
}