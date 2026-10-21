package com.alorma.caducity.domain.model

import com.alorma.caducity.config.time.date
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** What the second line of a product row says after the quantity: only what the screen does not already say. */
sealed interface ProductRowDetail {
  /** Lists grouped by day (Home, calendar): «1 unitat · Làctics», in plain grey. */
  data class InCategory(
    val categoryName: String,
  ) : ProductRowDetail

  /** Lists grouped by category or status: «2 unitats · D'aquí 2 dies», bold in the status color. */
  data object WithDays : ProductRowDetail

  /** Calendar opened from a category: the quantity alone. */
  data object QuantityOnly : ProductRowDetail
}

/**
 * One row of any product list. [itemIds] are the units of the row ordered by [sortedNearestFirst]: the
 * round buttons act on the first one.
 */
data class ProductRowModel(
  val key: String,
  val name: String,
  val itemIds: List<String>,
  val status: ExpiryStatus,
  /** Days until the nearest unit expires (negative when it already has); null for frozen units. */
  val remainingDays: Int?,
  /** Shown as the leading icon when the list mixes categories; null otherwise. */
  val category: Category?,
  val detail: ProductRowDetail,
  /** Day the unit that has been frozen the longest went into the freezer; null if the row is not frozen. */
  val frozenSince: LocalDate? = null,
  /** The product can go in the freezer: when false the row has no ❄. */
  val canFreeze: Boolean = true,
  val unitType: UnitType = UnitType.DEFAULT,
  /** Total amount of the batches of the row (g or mL); null for units. */
  val amount: Int? = null,
) {
  val unitCount: Int get() = itemIds.size
  val nearestItemId: String get() = itemIds.first()
}

/** Frozen units: the one that has been in the freezer the longest first (then the one that was to expire first). */
private val FROZEN_OLDEST_FIRST =
  compareBy<PantryEntry> { it.frozenSince ?: Instant.DISTANT_FUTURE }
    .thenBy { it.expiration }
    .thenBy { it.createdAt }
    .thenBy { it.itemId }

private fun List<PantryEntry>.onlyFrozen() = isNotEmpty() && all { it.status == ExpiryStatus.FROZEN }

private val NEAREST_FIRST =
  compareBy<PantryEntry> { it.expiration }
    .thenBy { it.createdAt }
    .thenBy { it.itemId }

/** Units ordered by the one that expires first: `(expiration, createdAt, id)`. */
fun List<PantryEntry>.sortedNearestFirst(): List<PantryEntry> =
  sortedWith(if (onlyFrozen()) FROZEN_OLDEST_FIRST else NEAREST_FIRST)

/** The unit the round buttons act on: the one that expires first. */
fun List<PantryEntry>.nearestFirst(): PantryEntry = minWith(if (onlyFrozen()) FROZEN_OLDEST_FIRST else NEAREST_FIRST)

/** Total of the batches of a measured product; null for units. */
fun List<PantryEntry>.sumAmount(): Int? = mapNotNull { it.amount }.takeIf { it.isNotEmpty() }?.sum()

/** Builds the row of one product from its units; the state and days are those of the nearest one. */
fun List<PantryEntry>.toProductRow(
  category: Category?,
  detail: ProductRowDetail,
  today: LocalDate,
): ProductRowModel {
  val sorted = sortedNearestFirst()
  val nearest = sorted.first()
  return ProductRowModel(
    key = nearest.groupKey,
    name = nearest.name,
    itemIds = sorted.map { it.itemId },
    status = nearest.status,
    // PantryEntry.remainingDays is the time frozen units have left, not the days to the date.
    remainingDays = if (nearest.status == ExpiryStatus.FROZEN) null else today.daysUntil(nearest.expiration),
    category = category,
    detail = detail,
    frozenSince = if (nearest.status == ExpiryStatus.FROZEN) nearest.frozenSince?.date() else null,
    canFreeze = nearest.canFreeze,
    unitType = nearest.unitType,
    amount = sumAmount(),
  )
}
