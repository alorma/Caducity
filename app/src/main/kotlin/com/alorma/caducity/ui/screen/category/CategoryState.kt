package com.alorma.caducity.ui.screen.category

import com.alorma.caducity.domain.model.CategoryData
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.usecase.WeekStripData
import kotlinx.datetime.LocalDate

/** What opens a category: the status filter and the product to highlight (old notifications). */
data class CategoryArgs(
  val categoryId: String,
  val filter: ExpiryStatus? = null,
  val highlightProductId: String? = null,
)

sealed interface CategoryState {
  data object Loading : CategoryState

  /** The category does not exist (any more). */
  data object NotFound : CategoryState

  data class Ready(
    val data: CategoryData,
    val weekStrip: WeekStripData,
    /** `null` shows every status. */
    val filter: ExpiryStatus?,
    /** Day picked in the week strip: its rows are highlighted, the list is not filtered (P19). */
    val selectedDay: LocalDate?,
    val highlightProductId: String?,
  ) : CategoryState {
    val visibleSections get() = data.sections.filter { filter == null || it.status == filter }
  }
}

sealed interface CategoryNavigation {
  data class AddItem(
    val source: String,
  ) : CategoryNavigation

  data object Calendar : CategoryNavigation

  data object Edit : CategoryNavigation

  data class ItemActions(
    val itemId: String,
  ) : CategoryNavigation
}

sealed interface CategoryNavigationSideEffect {
  data object OpenAdd : CategoryNavigationSideEffect

  data object OpenCalendar : CategoryNavigationSideEffect

  data object OpenEditor : CategoryNavigationSideEffect

  data class OpenItemActions(
    val itemId: String,
  ) : CategoryNavigationSideEffect

  /** The category was deleted: leave the screen. */
  data object Close : CategoryNavigationSideEffect
}

sealed interface CategorySideEffect {
  data class ShowDeleteConfirm(
    val name: String,
    val unitCount: Int,
  ) : CategorySideEffect

  data object DeleteFailed : CategorySideEffect
}
