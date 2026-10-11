package com.alorma.caducity.domain.usecase

import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow

interface ExpirationThresholds {
  /** "Soon" threshold in days (1, 3 or 7); reactive so statuses are recomputed when it changes. */
  val soonThresholdDays: Flow<Int>

  // TEMP(fase 5): used by the old screens; removed with them.
  val soonExpiringThreshold: Duration
}
