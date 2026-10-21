package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.LocalDate

/**
 * Active items with their status. The status is recomputed whenever the data, the global "soon"
 * threshold or the day changes, so it is never stale after midnight.
 */
class ObserveItemsUseCase(
  private val pantryDataSource: PantryDataSource,
  private val expirationThresholds: ExpirationThresholds,
  private val todayProvider: TodayProvider,
) {
  fun observe(): Flow<List<PantryEntry>> =
    combine(
      pantryDataSource.observeActiveItems(),
      expirationThresholds.soonThresholdDays,
      todayProvider.observe(),
    ) { items, soonDays, today ->
      items.map { it.toEntry(today, soonDays) }
    }.flowOn(Dispatchers.Default)

  private fun ActiveItem.toEntry(
    today: LocalDate,
    soonDays: Int,
  ) = PantryEntry(
    itemId = itemId,
    productId = productId,
    name = name,
    categoryId = categoryId,
    expiration = expiration,
    status = ExpiryStatus.of(expiration, today, soonDays, frozen),
    frozenSince = frozenSince,
    remainingDays = remainingDays,
    createdAt = createdAt,
    canFreeze = canFreeze,
    unitType = unitType,
    amount = amount,
    level = level,
  )
}
