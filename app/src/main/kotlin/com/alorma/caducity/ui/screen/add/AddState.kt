package com.alorma.caducity.ui.screen.add

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.NameSuggestion
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

data class AddState(
  val today: LocalDate,
  val name: String = "",
  val suggestion: NameSuggestion? = null,
  /** Name typed and not in the history: the screen asks for category and date. */
  val isNewProduct: Boolean = false,
  val categories: List<Category> = emptyList(),
  val categoryId: String? = null,
  val dateDigits: String = "",
  /** Parsed from [dateDigits]; null while incomplete or invalid. */
  val date: LocalDate? = null,
  /** Usual duration of the category when [date] is the suggestion built from it; null once the user sets the date. */
  val suggestedDurationDays: Int? = null,
  val quantity: Int = 1,
  val frozen: Boolean = false,
  val saving: Boolean = false,
  /** Id of the unit being edited; null when adding a new product. */
  val editItemId: String? = null,
) {
  val isEditing: Boolean get() = editItemId != null

  val missing: List<AddMissing>
    get() =
      buildList {
        if (name.isBlank()) add(AddMissing.Name)
        if (categoryId == null) add(AddMissing.Category)
        if (date == null) add(AddMissing.Date)
      }

  val canSave: Boolean get() = missing.isEmpty() && !saving

  val frozenWithPastDate: Boolean get() = frozen && date != null && date < today
}
