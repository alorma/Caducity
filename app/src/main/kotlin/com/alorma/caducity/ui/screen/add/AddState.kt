package com.alorma.caducity.ui.screen.add

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.NameSuggestion
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import kotlinx.datetime.LocalDate

/** What is still needed before the product can be saved. */
enum class AddMissing {
  Name,
  Category,
  Date,
}

/** Arguments of the Add route. [itemId] opens the screen editing that unit. */
data class AddArgs(
  val categoryId: String? = null,
  val itemId: String? = null,
)

/** Name, category and date of the unit as it was loaded to be edited. */
data class EditOriginal(
  val name: String,
  val categoryId: String,
  val date: LocalDate,
)

data class AddState(
  val today: LocalDate,
  val name: String = "",
  val suggestion: NameSuggestion? = null,
  /** Name typed and not in the history: the screen asks for category and date. */
  val isNewProduct: Boolean = false,
  /** The typed name is exactly one in the history: its category and date come from it. */
  val fromHistory: Boolean = false,
  val categories: List<Category> = emptyList(),
  val categoryId: String? = null,
  val dateDigits: String = "",
  /** Parsed from [dateDigits]; null while incomplete or invalid. */
  val date: LocalDate? = null,
  /** Usual duration of the category when [date] is the suggestion built from it; null once the user sets the date. */
  val suggestedDurationDays: Int? = null,
  val quantity: Int = 1,
  /** Amount (g or mL) of a measured product; meaningless for the other types. */
  val amount: Int = UnitType.DEFAULT_AMOUNT,
  /** Level of a handful product (Grapats). */
  val level: HandfulLevel = HandfulLevel.DEFAULT,
  val frozen: Boolean = false,
  val saving: Boolean = false,
  /** Id of the unit being edited; null when adding a new product. */
  val editItemId: String? = null,
  /** What the unit being edited had when the screen opened; «Desa» only enables once something differs. */
  val original: EditOriginal? = null,
  /** Day the unit being edited went into the freezer; null if it is not frozen. */
  val frozenOn: LocalDate? = null,
  /** Override of «es pot congelar» chosen by the user in this session; null while untouched. */
  val canFreezeOverride: Boolean? = null,
  /** Override stored in the existing product that matches the typed name in the selected category; null if none. */
  val productCanFreeze: Boolean? = null,
  /** Unit type chosen by the user in this session; null while untouched. */
  val unitTypeOverride: UnitType? = null,
  /** Unit type of the existing product that matches the typed name in the selected category; null if none. */
  val productUnitType: UnitType? = null,
) {
  val isEditing: Boolean get() = editItemId != null

  /** The selected category; null until one is chosen. */
  val category: Category? get() = categories.firstOrNull { it.id == categoryId }

  /** Effective «es pot congelar»: user's choice, else the existing product's override, else the category's. */
  val canFreeze: Boolean? get() = category?.let { canFreezeOverride ?: productCanFreeze ?: it.canFreeze }

  /** How the product is counted: user's choice, else the existing product's, else units. */
  val unitType: UnitType get() = unitTypeOverride ?: productUnitType ?: UnitType.DEFAULT

  val isMeasured: Boolean get() = unitType.parent == UnitParent.MEASURED

  val isHandful: Boolean get() = unitType.parent == UnitParent.HANDFULS

  /** The type chosen is of another parent than the existing product: saving resets its quantity. */
  val resetsQuantity: Boolean get() = productUnitType != null && unitType.parent != productUnitType.parent

  /** What is saved as [frozen]: a product that cannot be frozen is never added frozen. */
  val frozenEffective: Boolean get() = frozen && canFreeze != false

  val missing: List<AddMissing>
    get() =
      buildList {
        if (name.isBlank()) add(AddMissing.Name)
        if (categoryId == null) add(AddMissing.Category)
        if (date == null) add(AddMissing.Date)
      }

  private val unitTypeChanged: Boolean
    get() = unitTypeOverride != null && unitTypeOverride != (productUnitType ?: UnitType.DEFAULT)

  private val canFreezeChanged: Boolean
    get() = canFreezeOverride != null && canFreezeOverride != (productCanFreeze ?: category?.canFreeze)

  /** Editing: something differs from what the unit had when the screen opened. */
  val hasChanges: Boolean
    get() =
      original?.let {
        name.trim() != it.name || categoryId != it.categoryId || date != it.date || unitTypeChanged || canFreezeChanged
      } ?: false

  val canSave: Boolean get() = missing.isEmpty() && !saving && (!isEditing || hasChanges)

  val frozenWithPastDate: Boolean get() = frozenEffective && date != null && date < today
}
