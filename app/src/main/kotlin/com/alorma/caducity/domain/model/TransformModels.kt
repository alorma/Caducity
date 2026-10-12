package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate

/** The units of an ingredient that a transformation spends (already chosen, soonest-to-expire first). */
data class TransformIngredient(
  val itemIds: List<String>,
)

/** One product created by a transformation: [servings] independent items, [frozenCount] of them frozen. */
data class TransformResult(
  val name: String,
  val servings: Int,
  val categoryId: String,
  val expiration: LocalDate,
  val frozenCount: Int,
)

/** Default date of a result: the one of the ingredient that expires first; it never gets longer on its own. */
fun defaultResultExpiration(ingredientExpirations: List<LocalDate>): LocalDate? = ingredientExpirations.minOrNull()
