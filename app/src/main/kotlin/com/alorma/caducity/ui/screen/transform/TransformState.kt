package com.alorma.caducity.ui.screen.transform

import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.NameSuggestion
import com.alorma.caducity.domain.model.defaultResultExpiration
import kotlinx.datetime.LocalDate

/** Arguments of the Transform route: the units that start as ingredients (several units of a product count as `N de M`). */
data class TransformArgs(
  val itemIds: List<String> = emptyList(),
)

/** A product used as ingredient: [count] of its [units] (soonest to expire first) are spent. */
data class IngredientUi(
  val key: String,
  val name: String,
  val categoryId: String,
  val units: List<ActiveItem>,
  val count: Int,
  /** Units the user selected explicitly; they are spent before the rest of the product. */
  val preferredIds: Set<String> = emptySet(),
) {
  val available: Int get() = units.size
  val spent: List<ActiveItem>
    get() = units.sortedBy { it.itemId !in preferredIds }.take(count)
}

/** A product the transformation creates. Category and date follow the ingredients until the user overrides them. */
data class ResultUi(
  val id: Int,
  val name: String = "",
  val servings: Int = 1,
  val categoryOverride: String? = null,
  /** Category this name already has in the history. */
  val historyCategoryId: String? = null,
  val expirationOverride: LocalDate? = null,
  val frozenCount: Int = 0,
  val suggestion: NameSuggestion? = null,
)

data class TransformState(
  val today: LocalDate,
  val ingredients: List<IngredientUi> = emptyList(),
  val results: List<ResultUi> = listOf(ResultUi(id = 0)),
  val categories: List<Category> = emptyList(),
  val expandedResultId: Int = 0,
  val saving: Boolean = false,
  /** Ingredient rows already folded away by the closing cascade that follows a successful save. */
  val closedRows: Int = 0,
  /** Free entry: opened without ingredients, so they are optional and category and date are chosen by hand. */
  val freeEntry: Boolean = false,
) {
  private val spentWithOwner: List<Pair<IngredientUi, ActiveItem>>
    get() = ingredients.flatMap { ingredient -> ingredient.spent.map { ingredient to it } }

  /** Ingredient that expires first among the spent units; frozen units have no real date. */
  val firstToExpire: IngredientUi?
    get() = spentWithOwner.filterNot { (_, unit) -> unit.frozen }.minByOrNull { (_, unit) -> unit.expiration }?.first

  /** Date of the ingredient that expires first; it never gets longer on its own (TODO(transforma-open-2): expired ones start today). */
  val defaultExpiration: LocalDate
    get() {
      val first =
        defaultResultExpiration(
          spentWithOwner
            .filterNot { (_, unit) ->
              unit.frozen
            }.map { it.second.expiration },
        )
      return first?.coerceAtLeast(today) ?: today
    }

  private val defaultCategoryId: String? get() = ingredients.firstOrNull()?.categoryId

  fun categoryOf(result: ResultUi): String? =
    listOf(result.categoryOverride, result.historyCategoryId, defaultCategoryId)
      .firstOrNull { id -> id != null && categories.any { it.id == id } }

  /** Null while there are no ingredients to take a default from and the user has not picked one. */
  fun expirationOf(result: ResultUi): LocalDate? =
    result.expirationOverride ?: defaultExpiration.takeIf { ingredients.isNotEmpty() }

  /** A result that is entirely frozen has no date, so it does not need one. */
  private fun hasRequiredDate(result: ResultUi): Boolean =
    expirationOf(result) != null || result.frozenCount == result.servings

  /** The date to save: a fully frozen result without one starts today. */
  fun expirationToSave(result: ResultUi): LocalDate = expirationOf(result) ?: today

  val canSave: Boolean
    get() =
      !saving &&
        (ingredients.isNotEmpty() || freeEntry) &&
        results.isNotEmpty() &&
        results.all { it.name.isNotBlank() && categoryOf(it) != null && hasRequiredDate(it) }
}
