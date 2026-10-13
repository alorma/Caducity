package com.alorma.caducity.ui.screen.settings.margins

import com.alorma.caducity.domain.model.Category

data class CategoryMarginRow(
  val category: Category,
  /** The stored usual duration in days, or `null` when the category has none. */
  val usualDays: Int?,
  /** The suggested duration when the category is one of the suggested ones. */
  val suggestedDays: Int?,
)

sealed interface CategoryMarginsState {
  data object Loading : CategoryMarginsState

  data class Ready(
    val rows: List<CategoryMarginRow>,
    val expandedId: String?,
  ) : CategoryMarginsState
}

sealed interface CategoryMarginsSideEffect {
  data object ShowRestoreConfirm : CategoryMarginsSideEffect

  data object Restored : CategoryMarginsSideEffect

  data object Failed : CategoryMarginsSideEffect
}
