package com.alorma.caducity.ui.screen.settings.margins

import com.alorma.caducity.domain.model.Category

data class CategoryMarginRow(
  val category: Category,
  /** The margin of its own, or `null` when it follows the default. */
  val ownDays: Int?,
  /** The margin that applies: the own one or the default. */
  val effectiveDays: Int,
  /** The suggested margin when the category is one of the suggested ones. */
  val suggestedDays: Int?,
)

sealed interface CategoryMarginsState {
  data object Loading : CategoryMarginsState

  data class Ready(
    val rows: List<CategoryMarginRow>,
    val defaultDays: Int,
    val expandedId: String?,
  ) : CategoryMarginsState
}

sealed interface CategoryMarginsSideEffect {
  data object ShowRestoreConfirm : CategoryMarginsSideEffect

  data object Restored : CategoryMarginsSideEffect

  data object Failed : CategoryMarginsSideEffect
}
