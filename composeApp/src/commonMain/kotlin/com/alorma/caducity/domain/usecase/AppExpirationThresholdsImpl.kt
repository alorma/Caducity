package com.alorma.caducity.domain.usecase

import com.russhwolf.settings.Settings
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class AppExpirationThresholdsImpl(
  settings: Settings
): ExpirationThresholds {
  override val soonExpiringThreshold: Duration = 4.days
}