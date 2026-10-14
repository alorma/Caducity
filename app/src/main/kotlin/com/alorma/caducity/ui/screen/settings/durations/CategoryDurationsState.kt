package com.alorma.caducity.ui.screen.settings.durations

import com.alorma.caducity.domain.model.Category

data class CategoryDurationRow(
  val category: Category,
  /** The stored usual duration in days, or `null` when the category has none. */
  val usualDays: Int?,
  /** The suggested duration when the category is one of the suggested ones. */
  val suggestedDays: Int?,
)

sealed interface CategoryDurationsState {
  data object Loading : CategoryDurationsState

  data class Ready(
    val rows: List<CategoryDurationRow>,
    val expandedId: String?,
  ) : CategoryDurationsState
}

sealed interface CategoryDurationsSideEffect {
  data object ShowRestoreConfirm : CategoryDurationsSideEffect

  data object Restored : CategoryDurationsSideEffect

  data object Failed : CategoryDurationsSideEffect
}
