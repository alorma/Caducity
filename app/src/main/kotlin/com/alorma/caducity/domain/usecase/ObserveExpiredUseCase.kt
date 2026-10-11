package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ExpiredCategoryGroup
import com.alorma.caducity.domain.model.ExpiredData
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.normalizeForSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Caducats: expired units grouped by category, the categories with most expired units first. */
class ObserveExpiredUseCase(
  private val observeItems: ObserveItemsUseCase,
  private val pantryDataSource: PantryDataSource,
  private val todayProvider: TodayProvider,
) {
  fun observe(): Flow<ExpiredData> =
    combine(
      observeItems.observe(),
      pantryDataSource.observeCategories(),
      todayProvider.observe(),
    ) { entries, categories, today ->
      val expired = entries.filter { it.status == ExpiryStatus.EXPIRED }
      val byCategory = expired.groupBy { it.categoryId }
      ExpiredData(
        today = today,
        totalCount = expired.size,
        groups =
          categories
            .mapNotNull { category ->
              val units = byCategory[category.id] ?: return@mapNotNull null
              ExpiredCategoryGroup(
                category = category,
                entries =
                  units.sortedWith(
                    compareBy({ it.expiration }, { it.name.normalizeForSearch() }, { it.itemId }),
                  ),
              )
            }.sortedByDescending { it.entries.size },
      )
    }
}
