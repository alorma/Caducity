package com.alorma.caducity.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** An active item with its computed status. Counts as one unit. */
data class PantryEntry(
  val itemId: String,
  val productId: String?,
  val name: String,
  val categoryId: String,
  val expiration: LocalDate,
  val status: ExpiryStatus,
  val frozenSince: Instant?,
  val remainingDays: Int?,
  val createdAt: Instant?,
) {
  /** Stable key of the product this unit belongs to (legacy items without product group by name). */
  val groupKey: String
    get() = productId ?: "legacy:$categoryId:${name.normalizeForSearch()}"
}

/** Units of one product, ordered by date; «Unitat N» is the position in [units]. */
data class ProductGroup(
  val key: String,
  val productId: String?,
  val name: String,
  val categoryId: String,
  val units: List<PantryEntry>,
) {
  val unitCount: Int get() = units.size
  val isSingle: Boolean get() = units.size == 1
  val nearest: PantryEntry get() = units.first()
}

/** Groups entries by product and sorts each group's units by (date, createdAt, id), nearest first. */
fun List<PantryEntry>.toProductGroups(): List<ProductGroup> =
  groupBy { it.groupKey }
    .map { (key, units) ->
      val sorted = units.sortedNearestFirst()
      ProductGroup(
        key = key,
        productId = sorted.first().productId,
        name = sorted.first().name,
        categoryId = sorted.first().categoryId,
        units = sorted,
      )
    }.sortedWith(
      compareBy<ProductGroup> {
        it.nearest.expiration
      }.thenBy { it.name.normalizeForSearch() }.thenBy { it.key },
    )

// region Home

data class HomeRow(
  val key: String,
  val name: String,
  val category: Category,
  /** Units of this product that expire on the section's day, nearest first (see [sortedNearestFirst]). */
  val itemIds: List<String>,
) {
  val unitCount: Int get() = itemIds.size

  /** The unit that the ✓ and ❄ buttons act on: the closest one. */
  val nearestItemId: String get() = itemIds.first()
}

data class HomeDaySection(
  val date: LocalDate,
  val rows: List<HomeRow>,
  /** Status of every unit of the section: `SOON` in the «soon» window, `FRESH` in [HomeData.upcoming]. */
  val status: ExpiryStatus = ExpiryStatus.SOON,
) {
  /** Units, not rows (P6). */
  val unitCount: Int get() = rows.sumOf { it.unitCount }
}

data class HomeData(
  val today: LocalDate,
  val expiredCount: Int,
  val oldestExpiredDaysAgo: Int?,
  val soonCount: Int,
  val freshCount: Int,
  val frozenCount: Int,
  /** One section per day with units in the global «soon» window. */
  val sections: List<HomeDaySection>,
  /** No active item at all (empty pantry). */
  val isPantryEmpty: Boolean,
  /** Only when [sections] is empty: the next fresh expirations (first days with units, at most [UPCOMING_MAX_ROWS] rows). */
  val upcoming: List<HomeDaySection> = emptyList(),
  /** Rows of the upcoming days that did not fit in [upcoming]. */
  val upcomingMoreCount: Int = 0,
) {
  /** There are items but none is about to expire (and no section to show). */
  val nothingExpiresSoon: Boolean get() = !isPantryEmpty && sections.isEmpty()

  /** Something that is not expired can still be cooked: units that are soon, fresh or frozen. */
  val hasUsableProducts: Boolean get() = soonCount + freshCount + frozenCount > 0

  companion object {
    const val UPCOMING_DAYS = 3
    const val UPCOMING_MAX_ROWS = 8
  }
}

// endregion

// region Pantry

data class PantryCategory(
  val category: Category,
  val unitCount: Int,
  val expiredCount: Int,
  /** Units per status, only non-zero ones, in order Expired, Soon, Fresh, Frozen. */
  val statusCounts: Map<ExpiryStatus, Int>,
  /** The closest date among non-frozen units (may be in the past). */
  val nearest: LocalDate?,
)

data class PantryData(
  val today: LocalDate,
  val totalUnits: Int,
  val categories: List<PantryCategory>,
)

data class PantrySearchResult(
  val group: ProductGroup,
  val category: Category,
)

// endregion

// region Category

data class CategorySection(
  val status: ExpiryStatus,
  val groups: List<ProductGroup>,
) {
  val unitCount: Int get() = groups.sumOf { it.unitCount }
}

data class CategoryData(
  val today: LocalDate,
  val category: Category,
  val unitCount: Int,
  val nearest: LocalDate?,
  val statusCounts: Map<ExpiryStatus, Int>,
  /** Sections in order Expired, Soon, Fresh, Frozen; empty ones are left out. */
  val sections: List<CategorySection>,
)

// endregion

// region Status list

data class StatusCategoryGroup(
  val category: Category,
  /** One entry per unit, in the order of the status (see [com.alorma.caducity.domain.usecase.ObserveItemsByStatusUseCase]). */
  val entries: List<PantryEntry>,
)

data class StatusListData(
  val status: ExpiryStatus,
  val today: LocalDate,
  val totalCount: Int,
  /** Ordered by number of units, descending. */
  val groups: List<StatusCategoryGroup>,
) {
  val isEmpty: Boolean get() = totalCount == 0
}

// endregion

internal val STATUS_ORDER = listOf(ExpiryStatus.EXPIRED, ExpiryStatus.SOON, ExpiryStatus.FRESH, ExpiryStatus.FROZEN)

// region Units

/** «Unitat [index] de [count]»: position of a unit among all the active units of its product. */
data class UnitPosition(
  val index: Int,
  val count: Int,
)

/** Position of every unit, ordered by `(expiration, createdAt, id)` within its product (stable and derived). */
fun List<PantryEntry>.unitPositions(): Map<String, UnitPosition> =
  groupBy { it.groupKey }
    .values
    .flatMap { units ->
      units
        .sortedWith(compareBy({ it.expiration }, { it.createdAt }, { it.itemId }))
        .mapIndexed { index, unit -> unit.itemId to UnitPosition(index + 1, units.size) }
    }.toMap()

// endregion
