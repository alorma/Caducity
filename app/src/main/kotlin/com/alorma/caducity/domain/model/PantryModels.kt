package com.alorma.caducity.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** Result of adding units of a product. */
data class AddedItems(
  val productId: String,
  val itemIds: List<String>,
  val productCreated: Boolean,
)

/** Usage stats of a product (any item, including consumed and discarded). */
data class ProductUsage(
  val productId: String,
  val name: String,
  val categoryId: String,
  val uses: Int,
  val lastUsed: Instant,
  /** The product's own override of `canFreeze`; null inherits its category. */
  val canFreeze: Boolean? = null,
  val unitType: UnitType = UnitType.DEFAULT,
)

/** A product name offered while typing in the add screen. */
data class NameSuggestion(
  val productId: String,
  val name: String,
  val categoryId: String,
  /** The product's own override of `canFreeze`; null inherits its category. */
  val canFreeze: Boolean? = null,
)

sealed class PantryError(
  message: String,
) : Exception(message) {
  data object BlankName : PantryError("Name cannot be blank")

  data class InvalidQuantity(
    val quantity: Int,
  ) : PantryError("Quantity out of range: $quantity")

  data object PastDateWhenFrozen : PantryError("A frozen item cannot have a past date")

  data object ItemNotFound : PantryError("Item not found")

  /** A level was asked of a batch that is not a handful. */
  data object NotAHandful : PantryError("The batch is not a handful product")

  data class InvalidAmount(
    val amount: Int,
  ) : PantryError("Amount out of range: $amount")

  data object DuplicateCategoryName : PantryError("A category with that name already exists")

  data object CategoryNotFound : PantryError("Category not found")

  data object CannotFreeze : PantryError("The batch cannot be frozen")
}

/** An active item as stored, with its product name and local expiration date (no status yet). */
data class ActiveItem(
  val itemId: String,
  val productId: String?,
  val name: String,
  val categoryId: String,
  val expiration: LocalDate,
  val frozen: Boolean,
  /** When the unit was put in the freezer; null if it is not frozen. */
  val frozenSince: Instant?,
  val remainingDays: Int?,
  val createdAt: Instant?,
  /** Effective value: the product's override, else the category's. */
  val canFreeze: Boolean = true,
  val unitType: UnitType = UnitType.DEFAULT,
  /** Amount of a measured batch (g or mL); null for units. */
  val amount: Int? = null,
  /** Level of a handful batch; null for the other types. */
  val level: HandfulLevel? = null,
)

/** Result of freezing part of a measured batch: [itemId] is the frozen batch, [created] when it is a new one split off. */
data class FrozenAmount(
  val itemId: String,
  val created: Boolean,
)
