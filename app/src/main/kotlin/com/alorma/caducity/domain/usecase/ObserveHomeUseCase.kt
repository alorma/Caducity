package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.HomeData
import com.alorma.caducity.domain.model.HomeDaySection
import com.alorma.caducity.domain.model.HomeRow
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.model.productCount
import com.alorma.caducity.domain.model.sortedNearestFirst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
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

      fun rowsOf(dayEntries: List<PantryEntry>): List<HomeRow> =
        dayEntries
          .groupBy { it.groupKey }
          .values
          .mapNotNull { units -> categoriesById[units.first().categoryId]?.let { toRow(units, it) } }
          .sortedWith(compareBy({ it.name.normalizeForSearch() }, { it.key }))

      val sections =
        byStatus[ExpiryStatus.SOON]
          .orEmpty()
          .groupBy { it.expiration }
          .toSortedMap()
          .map { (date, dayEntries) -> HomeDaySection(date = date, rows = rowsOf(dayEntries)) }
          .filter { it.rows.isNotEmpty() }

      val upcomingDays =
        if (sections.isNotEmpty()) {
          emptyList()
        } else {
          byStatus[ExpiryStatus.FRESH]
            .orEmpty()
            .groupBy { it.expiration }
            .toSortedMap()
            .entries
            .take(HomeData.UPCOMING_DAYS)
            .map { (date, dayEntries) -> HomeDaySection(date, rowsOf(dayEntries), ExpiryStatus.FRESH) }
        }
      var roomLeft = HomeData.UPCOMING_MAX_ROWS
      val upcoming =
        upcomingDays
          .map { section ->
            val kept = section.rows.take(roomLeft)
            roomLeft -= kept.size
            section.copy(rows = kept)
          }.filter { it.rows.isNotEmpty() }

      HomeData(
        today = today,
        expiredCount = expired.productCount(),
        oldestExpiredDaysAgo = expired.minOfOrNull { it.expiration }?.let { it.daysUntil(today) },
        soonCount = byStatus[ExpiryStatus.SOON].orEmpty().productCount(),
        freshCount = byStatus[ExpiryStatus.FRESH].orEmpty().productCount(),
        frozenCount = byStatus[ExpiryStatus.FROZEN].orEmpty().productCount(),
        sections = sections,
        isPantryEmpty = entries.isEmpty(),
        upcoming = upcoming,
        upcomingMoreCount = upcomingDays.sumOf { it.rows.size } - upcoming.sumOf { it.rows.size },
      )
    }.flowOn(Dispatchers.Default)

  private fun toRow(
    units: List<PantryEntry>,
    category: com.alorma.caducity.domain.model.Category,
  ): HomeRow {
    val sorted = units.sortedNearestFirst()
    val nearest = sorted.first()
    return HomeRow(
      key = nearest.groupKey,
      name = nearest.name,
      category = category,
      itemIds = sorted.map { it.itemId },
      canFreeze = nearest.canFreeze,
    )
  }
}
