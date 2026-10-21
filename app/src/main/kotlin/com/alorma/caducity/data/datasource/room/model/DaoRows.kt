package com.alorma.caducity.data.datasource.room.model

import com.alorma.caducity.domain.model.UnitType

/** Number of active (not consumed, not discarded) items of a category. */
data class CategoryActiveCountRow(
  val categoryId: String,
  val activeCount: Int,
)

/** A product with how many times it was used and when it was last used, for name suggestions. */
data class ProductUsageRow(
  val productId: String,
  val name: String,
  val categoryId: String,
  val uses: Int,
  val lastUsed: Long,
  /** The product's own override of `canFreeze`; null inherits its category. */
  val canFreeze: Boolean? = null,
  val unitType: UnitType = UnitType.DEFAULT,
)

/** An active item joined with its product name (falls back to the legacy `identifier`). */
data class ActiveItemRow(
  val itemId: String,
  val categoryId: String,
  val productId: String?,
  val name: String,
  val expirationDate: Long,
  val pausedDate: Long?,
  val remainingDays: Int?,
  val createdAt: Long?,
  /** Effective value: the product's override, else the category's. */
  val canFreeze: Boolean,
  /** How the product is counted; a unit without product is always counted in units. */
  val unitType: UnitType,
  /** Amount of a measured batch (g or mL); null for units. */
  val amount: Int?,
)
