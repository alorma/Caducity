package com.alorma.caducity.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** An active item with its computed status. Counts as one unit, even a legacy pack (P17). */
data class PantryEntry(
  val itemId: String,
  val productId: String?,
  val name: String,
  val categoryId: String,
  val expiration: LocalDate,
  val status: ExpiryStatus,
  val frozenSince: Instant?,
  val remainingDays: Int?,
  val packSize: Int?,
  val createdAt: Instant?,
  /** Effective "soon" margin in days: the category's own or the global default. */
  val soonDays: Int,
) {
  /** Stable key of the product this unit belongs to (legacy items without product group by name). */
  val groupKey: String
    get() = productId ?: "legacy:$categoryId:${name.normalizeForSearch()}"

  /** Legacy pack size, shown as «pack de N»; null for regular units. */
  val legacyPack: Int?
    get() = packSize?.takeIf { it > 1 }
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

private val UNIT_ORDER =
  compareBy<PantryEntry> { it.expiration }
    .thenBy { it.createdAt }
    .thenBy { it.itemId }

/** Groups entries by product and sorts each group's units by (date, createdAt, id), stable «Unitat N». */
fun List<PantryEntry>.toProductGroups(): List<ProductGroup> =
  groupBy { it.groupKey }
    .map { (key, units) ->
      val sorted = units.sortedWith(UNIT_ORDER)
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

/** Days shown as sections in Home (today + 6), the same window as the week strip. */
const val HOME_SECTION_DAYS = 7

data class HomeRow(
  val key: String,
  val name: String,
  val category: Category,
  /** Units of this product that expire on the section's day. */
  val unitCount: Int,
  /** The unit that the ✓ button consumes: the closest one. */
  val nearestItemId: String,
  val legacyPack: Int?,
)

data class HomeDaySection(
  val date: LocalDate,
  val rows: List<HomeRow>,
) {
  /** Units, not rows (P6). */
  val unitCount: Int get() = rows.sumOf { it.unitCount }
}

/** A "soon" product beyond the day sections: it entered "soon" through its category's margin. */
data class HomeLaterRow(
  val date: LocalDate,
  val row: HomeRow,
)

/** The units that are "soon" but expire after the last day section, folded into «Més endavant». */
data class HomeLater(
  /** Ordered by date, then name. */
  val rows: List<HomeLaterRow>,
  /** Categories involved with their effective margin, shortest margin first. */
  val margins: List<HomeLaterMargin>,
) {
  val unitCount: Int get() = rows.sumOf { it.row.unitCount }
  val firstDate: LocalDate get() = rows.first().date
  val lastDate: LocalDate get() = rows.last().date
}

data class HomeLaterMargin(
  val category: Category,
  val days: Int,
)

data class HomeData(
  val today: LocalDate,
  val expiredCount: Int,
  val oldestExpiredDaysAgo: Int?,
  val soonCount: Int,
  val freshCount: Int,
  val frozenCount: Int,
  /** Day sections from today to today + [HOME_SECTION_DAYS] - 1. */
  val sections: List<HomeDaySection>,
  val later: HomeLater?,
  /** No active item at all (empty pantry). */
  val isPantryEmpty: Boolean,
) {
  /** There are items but none is about to expire (and no section to show). */
  val nothingExpiresSoon: Boolean get() = !isPantryEmpty && sections.isEmpty() && later == null
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
  /** «Unitat N de M» of every active unit of the category, by item id. */
  val positions: Map<String, UnitPosition>,
)

// endregion

// region Status list

data class StatusCategoryGroup(
  val category: Category,
  /** One entry per unit, in the order of the status (see [com.alorma.caducity.domain.usecase.ObserveItemsByStatusUseCase]). */
  val entries: List<PantryEntry>,
) {
  /** Effective "soon" margin of the category (all its entries share it). */
  val soonDays: Int? get() = entries.firstOrNull()?.soonDays
}

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
