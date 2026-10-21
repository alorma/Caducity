package com.alorma.caducity.ui.screen.transform

import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.NameSuggestion
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
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
  /** Status of each unit, with the margins of its category, so the card is coloured like the rest of the app. */
  val unitStatus: Map<String, ExpiryStatus> = emptyMap(),
  /** g or mL to spend of a measured product; null for units. */
  val amount: Int? = null,
  /** Level a handful product is left at after the transformation; null when nothing is left (spent whole). */
  val remainingLevel: HandfulLevel? = null,
) {
  /** A handful product (Grapats): the user picks what is left of it. */
  val handful: Boolean get() = units.firstOrNull()?.unitType?.parent == UnitParent.HANDFULS

  /** Level of the batch now. */
  val currentLevel: HandfulLevel? get() = units.firstOrNull()?.level

  /** What it can be left at: nothing, or any level below the current one (lowest first). */
  val remainingOptions: List<HandfulLevel?>
    get() {
      val current = currentLevel ?: return listOf(null)
      return listOf<HandfulLevel?>(null) + HandfulLevel.entries.reversed().filter { it.ordinal > current.ordinal }
    }

  /** Everything a measured product has, in g or mL. */
  val totalAmount: Int get() = units.sumOf { it.amount ?: 0 }

  val unitType: UnitType get() = units.firstOrNull()?.unitType ?: UnitType.DEFAULT

  val available: Int get() = units.size

  /** A measured product (grams, millilitres) has an amount to spend instead of `N de M`. */
  val measured: Boolean get() = units.firstOrNull()?.unitType?.parent == UnitParent.MEASURED
  val spent: List<ActiveItem>
    get() = units.sortedBy { it.itemId !in preferredIds }.take(count)

  /** Status of the unit that is spent first: the one that sets the colour of the card and of its line. */
  val status: ExpiryStatus?
    get() = spent.firstOrNull()?.let { unitStatus[it.itemId] }
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
  /** `canFreeze` override of the existing products with this exact name, by category (null inherits the category). */
  val productCanFreeze: Map<String, Boolean?> = emptyMap(),
  val suggestion: NameSuggestion? = null,
  /** Unit type chosen by the user; null while untouched (units for a new product, the existing type otherwise). */
  val unitTypeOverride: UnitType? = null,
  /** Level of a handful result. */
  val level: HandfulLevel = HandfulLevel.DEFAULT,
  /** g or mL of a measured result (a single batch). */
  val amount: Int = UnitType.DEFAULT_AMOUNT,
) {
  val unitType: UnitType get() = unitTypeOverride ?: UnitType.DEFAULT
  val measured: Boolean get() = unitType.parent == UnitParent.MEASURED
  val handful: Boolean get() = unitType.parent == UnitParent.HANDFULS
}

/** `expandedResultId` when every result is folded into a small card. */
internal const val NONE_EXPANDED = -1

data class TransformState(
  val today: LocalDate,
  val ingredients: List<IngredientUi> = emptyList(),
  val results: List<ResultUi> = listOf(ResultUi(id = 0)),
  val categories: List<Category> = emptyList(),
  val expandedResultId: Int = 0,
  /** The result whose name takes the focus when its form opens empty: only the one just added, never on entering. */
  val focusResultId: Int? = null,
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

  /** Date of the ingredient that expires first; it never gets longer on its own, and an expired one starts today. */
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

  /**
   * Whether the result can go in the freezer: the override of an existing product with its name in the chosen category,
   * else the category's own value.
   */
  fun canFreezeOf(result: ResultUi): Boolean {
    val category = categories.firstOrNull { it.id == categoryOf(result) } ?: return true
    return result.productCanFreeze[category.id] ?: category.canFreeze
  }

  /** How many go to the freezer: none when the result cannot be frozen. */
  fun frozenCountOf(result: ResultUi): Int = if (canFreezeOf(result)) result.frozenCount else 0

  /** A result that is entirely frozen has no date, so it does not need one. */
  private fun hasRequiredDate(result: ResultUi): Boolean =
    expirationOf(result) != null || frozenCountOf(result) == result.servings

  /** The date to save: a fully frozen result without one starts today. */
  fun expirationToSave(result: ResultUi): LocalDate = expirationOf(result) ?: today

  /** What a result needs to be saved: a name, a category and a date (unless it is entirely frozen). */
  fun isValid(result: ResultUi): Boolean =
    result.name.isNotBlank() && categoryOf(result) != null && hasRequiredDate(result)

  /** A new result is not allowed while one is still blank. */
  val canAddResult: Boolean
    get() = results.none { it.name.isBlank() }

  val canSave: Boolean
    get() = !saving && ingredients.isNotEmpty() && results.isNotEmpty() && results.all(::isValid)
}
