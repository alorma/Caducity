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
) {
  val available: Int get() = units.size
  val spent: List<ActiveItem> get() = units.take(count)
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

  fun expirationOf(result: ResultUi): LocalDate = result.expirationOverride ?: defaultExpiration

  val canSave: Boolean
    get() =
      !saving &&
        ingredients.isNotEmpty() &&
        results.isNotEmpty() &&
        results.all { it.name.isNotBlank() && categoryOf(it) != null }
}
