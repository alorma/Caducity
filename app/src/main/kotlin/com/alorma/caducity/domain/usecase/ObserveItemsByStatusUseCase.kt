package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.StatusCategoryGroup
import com.alorma.caducity.domain.model.StatusListData
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.model.productCount
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Units in one [ExpiryStatus] grouped by category (most units first). Rows are oldest date first,
 * except frozen ones: longest in the freezer first.
 */
class ObserveItemsByStatusUseCase(
  private val observeItems: ObserveItemsUseCase,
  private val pantryDataSource: PantryDataSource,
  private val todayProvider: TodayProvider,
) {
  fun observe(status: ExpiryStatus): Flow<StatusListData> =
    combine(
      observeItems.observe(),
      pantryDataSource.observeCategories(),
      todayProvider.observe(),
    ) { entries, categories, today ->
      val matching = entries.filter { it.status == status }
      val byCategory = matching.groupBy { it.categoryId }
      StatusListData(
        status = status,
        today = today,
        totalCount = matching.productCount(),
        groups =
          categories
            .mapNotNull { category ->
              val units = byCategory[category.id] ?: return@mapNotNull null
              StatusCategoryGroup(category = category, entries = units.sortedWith(rowOrder(status)))
            }.sortedByDescending { it.entries.size },
      )
    }

  private fun rowOrder(status: ExpiryStatus): Comparator<PantryEntry> =
    if (status == ExpiryStatus.FROZEN) {
      compareBy<PantryEntry> { it.frozenSince ?: Instant.DISTANT_FUTURE }
        .thenBy { it.name.normalizeForSearch() }
        .thenBy { it.itemId }
    } else {
      compareBy<PantryEntry> { it.expiration }
        .thenBy { it.name.normalizeForSearch() }
        .thenBy { it.itemId }
    }
}
