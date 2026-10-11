package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.HOME_SECTION_DAYS
import com.alorma.caducity.domain.model.HomeData
import com.alorma.caducity.domain.model.HomeDaySection
import com.alorma.caducity.domain.model.HomeLater
import com.alorma.caducity.domain.model.HomeLaterMargin
import com.alorma.caducity.domain.model.HomeLaterRow
import com.alorma.caducity.domain.model.HomeRow
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.normalizeForSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

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

      val lastSectionDay = today.plus(HOME_SECTION_DAYS - 1, DateTimeUnit.DAY)
      val (inWindow, beyond) = byStatus[ExpiryStatus.SOON].orEmpty().partition { it.expiration <= lastSectionDay }

      fun rowsOf(dayEntries: List<PantryEntry>): List<HomeRow> =
        dayEntries
          .groupBy { it.groupKey }
          .values
          .mapNotNull { units -> categoriesById[units.first().categoryId]?.let { toRow(units, it) } }
          .sortedWith(compareBy({ it.name.normalizeForSearch() }, { it.key }))

      val sections =
        inWindow
          .groupBy { it.expiration }
          .toSortedMap()
          .map { (date, dayEntries) -> HomeDaySection(date = date, rows = rowsOf(dayEntries)) }
          .filter { it.rows.isNotEmpty() }

      val laterRows =
        beyond
          .groupBy { it.expiration }
          .toSortedMap()
          .flatMap { (date, dayEntries) -> rowsOf(dayEntries).map { HomeLaterRow(date, it) } }
      val later =
        laterRows.takeIf { it.isNotEmpty() }?.let { rows ->
          HomeLater(
            rows = rows,
            margins =
              rows
                .map { it.row.category }
                .distinctBy { it.id }
                .map { category ->
                  HomeLaterMargin(category, beyond.first { it.categoryId == category.id }.soonDays)
                }.sortedWith(compareBy({ it.days }, { it.category.name.normalizeForSearch() })),
          )
        }

      HomeData(
        today = today,
        expiredCount = expired.size,
        oldestExpiredDaysAgo = expired.minOfOrNull { it.expiration }?.let { it.daysUntil(today) },
        soonCount = byStatus[ExpiryStatus.SOON].orEmpty().size,
        freshCount = byStatus[ExpiryStatus.FRESH].orEmpty().size,
        frozenCount = byStatus[ExpiryStatus.FROZEN].orEmpty().size,
        sections = sections,
        later = later,
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
