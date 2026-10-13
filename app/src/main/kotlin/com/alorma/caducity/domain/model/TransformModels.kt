package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate

/** The units of an ingredient that a transformation spends (already chosen, soonest-to-expire first). */
data class TransformIngredient(
  val itemIds: List<String>,
)

/**
 * An ingredient as the user picked it: spend [count] of its [candidateItemIds] (`1 de N`). The [preferredItemIds]
 * (the units the user explicitly selected) are spent first; within each group, soonest to expire first.
 */
data class IngredientSpend(
  val candidateItemIds: List<String>,
  val count: Int,
  val preferredItemIds: Set<String> = emptySet(),
)

/** One product created by a transformation: [servings] independent items, [frozenCount] of them frozen. */
data class TransformResult(
  val name: String,
  val servings: Int,
  val categoryId: String,
  val expiration: LocalDate,
  val frozenCount: Int,
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
