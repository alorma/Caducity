package com.alorma.caducity.domain.model

import kotlin.time.Instant

/** Result of adding units of a product: used to undo the add. */
data class AddedBatch(
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
)

/** A product name offered while typing in the add screen. */
data class NameSuggestion(
  val productId: String,
  val name: String,
  val categoryId: String,
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

  data object DuplicateCategoryName : PantryError("A category with that name already exists")

  data object CategoryNotFound : PantryError("Category not found")
}
