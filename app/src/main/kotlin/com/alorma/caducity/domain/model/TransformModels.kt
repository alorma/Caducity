package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate

/** The units of an ingredient that a transformation spends (already chosen, soonest-to-expire first). */
data class TransformIngredient(
  val itemIds: List<String>,
  /** Batches of a measured product that are spent only in part: item id to the g or mL taken from it. */
  val partialAmounts: Map<String, Int> = emptyMap(),
  /** Handful batches that are not spent whole: item id to the level it is left at. */
  val partialLevels: Map<String, HandfulLevel> = emptyMap(),
)

/**
 * An ingredient as the user picked it: spend [count] of its [candidateItemIds] (`1 de N`). The [preferredItemIds]
 * (the units the user explicitly selected) are spent first; within each group, soonest to expire first.
 */
data class IngredientSpend(
  val candidateItemIds: List<String>,
  val count: Int,
  val preferredItemIds: Set<String> = emptySet(),
  /** g or mL spent of a measured product (its batches, soonest first); [count] is then ignored. */
  val amount: Int? = null,
  /** A handful ingredient: [remainingLevel] is what is left of it afterwards, or null (nothing left) to spend it whole. */
  val handful: Boolean = false,
  val remainingLevel: HandfulLevel? = null,
)

/** One product created by a transformation: [servings] independent items, [frozenCount] of them frozen. */
data class TransformResult(
  val name: String,
  val servings: Int,
  val categoryId: String,
  val expiration: LocalDate,
  val frozenCount: Int,
  /** How the product is counted; null leaves an existing product as it is. A measured result is a single batch of [amount] g or mL ([servings] is then 1). */
  val unitType: UnitType? = null,
  val amount: Int? = null,
  /** Level of a handful result (a single batch). */
  val level: HandfulLevel? = null,
)

/** What a transformation did, enough to undo it: new items/products and the previous state of the ingredients. */
data class TransformedBatch(
  /** Null when there were no ingredients: the results are plain new products and no transformation exists. */
  val transformationId: String?,
  val resultItemIds: List<String>,
  val createdProductIds: List<String>,
  val ingredientSnapshots: List<ItemSnapshot>,
)

/** Default date of a result: the one of the ingredient that expires first; it never gets longer on its own. */
fun defaultResultExpiration(ingredientExpirations: List<LocalDate>): LocalDate? = ingredientExpirations.minOrNull()
