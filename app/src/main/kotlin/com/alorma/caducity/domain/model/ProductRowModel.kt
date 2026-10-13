package com.alorma.caducity.domain.model

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
) {
  val unitCount: Int get() = itemIds.size
  val nearestItemId: String get() = itemIds.first()
}

private val NEAREST_FIRST =
  compareBy<PantryEntry> { it.expiration }
    .thenBy { it.createdAt }
    .thenBy { it.itemId }

/** Units ordered by the one that expires first: `(expiration, createdAt, id)`. */
fun List<PantryEntry>.sortedNearestFirst(): List<PantryEntry> = sortedWith(NEAREST_FIRST)

/** The unit the round buttons act on: the one that expires first. */
fun List<PantryEntry>.nearestFirst(): PantryEntry = minWith(NEAREST_FIRST)

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
  )
}
