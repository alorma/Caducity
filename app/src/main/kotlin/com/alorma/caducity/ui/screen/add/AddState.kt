package com.alorma.caducity.ui.screen.add

import com.alorma.caducity.domain.model.AddedBatch
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.NameSuggestion
import kotlinx.datetime.LocalDate

/** What is still needed before the product can be saved. */
enum class AddMissing {
  Name,
  Category,
  Date,
}

/** A product saved in the current batch ("En aquesta tanda"). */
data class AddedEntry(
  val batch: AddedBatch,
  val name: String,
  val categoryId: String,
  val date: LocalDate,
  val quantity: Int,
)

/** Arguments of the Add route. [itemId] opens the screen editing that unit. */
data class AddArgs(
  val categoryId: String? = null,
  val itemId: String? = null,
)

/** The unit being edited (one unit, P20). [entry] is set when it was picked from «En aquesta tanda». */
data class EditTarget(
  val itemId: String,
  val entry: AddedEntry? = null,
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
  val quantity: Int = 1,
  val frozen: Boolean = false,
  val added: List<AddedEntry> = emptyList(),
  val saving: Boolean = false,
  val editing: EditTarget? = null,
) {
  val isEditing: Boolean get() = editing != null

  val isBatchMode: Boolean get() = added.isNotEmpty() && !isEditing

  val missing: List<AddMissing>
    get() =
      buildList {
        if (name.isBlank()) add(AddMissing.Name)
        if (categoryId == null) add(AddMissing.Category)
        if (date == null) add(AddMissing.Date)
      }

  /** In batch mode with the name empty, "Desa" becomes "Fet" and nothing else is needed. */
  val isDone: Boolean get() = isBatchMode && name.isBlank() && !isEditing

  val canSave: Boolean get() = missing.isEmpty() && !saving

  val frozenWithPastDate: Boolean get() = frozen && date != null && date < today
}
