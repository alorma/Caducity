package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.HomeData
import com.alorma.caducity.domain.model.HomeDaySection
import com.alorma.caducity.domain.model.HomeRow
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.normalizeForSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.daysUntil

/** «Què caduca»: counters per status and one row per (product, day) for the units that are about to expire. */
class ObserveHomeUseCase(
  private val observeItems: ObserveItemsUseCase,
  private val pantryDataSource: PantryDataSource,
  private val todayProvider: TodayProvider,
) {
  fun observe(): Flow<HomeData> =
    combine(
      observeItems.observe(),
      pantryDataSource.observeCategories(),
      todayProvider.observe(),
    ) { entries, categories, today ->
      val byStatus = entries.groupBy { it.status }
      val expired = byStatus[ExpiryStatus.EXPIRED].orEmpty()
      val categoriesById = categories.associateBy { it.id }

      val sections =
        byStatus[ExpiryStatus.SOON]
          .orEmpty()
          .groupBy { it.expiration }
          .toSortedMap()
          .map { (date, dayEntries) ->
            HomeDaySection(
              date = date,
              rows =
                dayEntries
                  .groupBy { it.groupKey }
                  .values
                  .mapNotNull { units -> categoriesById[units.first().categoryId]?.let { toRow(units, it) } }
                  .sortedWith(compareBy({ it.name.normalizeForSearch() }, { it.key })),
            )
          }.filter { it.rows.isNotEmpty() }

      HomeData(
        today = today,
        expiredCount = expired.size,
        oldestExpiredDaysAgo = expired.minOfOrNull { it.expiration }?.let { it.daysUntil(today) },
        soonCount = byStatus[ExpiryStatus.SOON].orEmpty().size,
        freshCount = byStatus[ExpiryStatus.FRESH].orEmpty().size,
        frozenCount = byStatus[ExpiryStatus.FROZEN].orEmpty().size,
        sections = sections,
        isPantryEmpty = entries.isEmpty(),
      )
    }

  private fun toRow(
    units: List<PantryEntry>,
    category: com.alorma.caducity.domain.model.Category,
  ): HomeRow {
    val sorted = units.sortedWith(compareBy({ it.createdAt }, { it.itemId }))
    val first = sorted.first()
    return HomeRow(
      key = first.groupKey,
      name = first.name,
      category = category,
      unitCount = units.size,
      nearestItemId = first.itemId,
      legacyPack = first.legacyPack.takeIf { units.size == 1 },
    )
  }
}
