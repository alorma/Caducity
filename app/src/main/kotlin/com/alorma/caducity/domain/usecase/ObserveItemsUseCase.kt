package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

/**
 * Active items with their status. The status is recomputed whenever the data, the "soon" margins
 * (global default or per category) or the day changes, so it is never stale after midnight.
 */
class ObserveItemsUseCase(
  private val pantryDataSource: PantryDataSource,
  private val expirationThresholds: ExpirationThresholds,
  private val todayProvider: TodayProvider,
) {
  fun observe(): Flow<List<PantryEntry>> =
    combine(
      pantryDataSource.observeActiveItems(),
      pantryDataSource.observeCategories(),
      expirationThresholds.soonThresholdDays,
      todayProvider.observe(),
    ) { items, categories, defaultDays, today ->
      val ownDays = categories.associate { it.id to it.soonThresholdDays }
      items.map { it.toEntry(today, ownDays[it.categoryId] ?: defaultDays) }
    }

  private fun ActiveItem.toEntry(
    today: LocalDate,
    thresholdDays: Int,
  ) = PantryEntry(
    itemId = itemId,
    productId = productId,
    name = name,
    categoryId = categoryId,
    expiration = expiration,
    status = ExpiryStatus.of(expiration, today, thresholdDays, frozen),
    frozenSince = frozenSince,
    remainingDays = remainingDays,
    packSize = packSize,
    createdAt = createdAt,
    soonDays = thresholdDays,
  )
}
