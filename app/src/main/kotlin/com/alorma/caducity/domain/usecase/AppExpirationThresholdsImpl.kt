package com.alorma.caducity.domain.usecase

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AppExpirationThresholdsImpl : ExpirationThresholds {
  // TEMP(fase 2, tasca 6): will read `SoonThresholdPreferences`.
  override val soonThresholdDays: Flow<Int> = flowOf(DEFAULT_SOON_THRESHOLD_DAYS)
  override val soonExpiringThreshold: Duration = 4.days
  override val consumeExpiredThreshold: Duration = 2.days

  companion object {
    const val DEFAULT_SOON_THRESHOLD_DAYS = 3
  }
}
