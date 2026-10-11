package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow

class AppExpirationThresholdsImpl(
  soonThresholdPreferences: SoonThresholdPreferences,
) : ExpirationThresholds {
  override val soonThresholdDays: Flow<Int> = soonThresholdPreferences.thresholdDays
  override val soonExpiringThreshold: Duration = 4.days
  override val consumeExpiredThreshold: Duration = 2.days
}
