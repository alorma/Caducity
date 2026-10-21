package com.alorma.caducity.ui.screen.category

import com.alorma.caducity.domain.model.CategoryData
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.usecase.WeekStripData
import com.alorma.caducity.ui.components.feedback.snackbar.SnackbarRequest
import kotlinx.datetime.LocalDate

/** What opens a category: the status filter and the product to highlight (old notifications). */
data class CategoryArgs(
  val categoryId: String,
  val filter: ExpiryStatus? = null,
  val highlightProductId: String? = null,
)

/** A unit listed under a selected day. */
data class DayUnit(
  val name: String,
  val unit: PantryEntry,
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

    /** Units that expire on [selectedDay], in one list (frozen ones have no effective date). */
    val dayUnits: List<DayUnit>
      get() {
        val day = selectedDay ?: return emptyList()
        return data.sections
          .filter { it.status != ExpiryStatus.FROZEN }
          .flatMap { it.groups }
          .flatMap { group ->
            group.units.filter { it.expiration == day }.map { unit ->
              DayUnit(group.name, unit)
            }
          }.sortedBy { it.name.lowercase() }
      }
  }
}

sealed interface CategoryNavigation {
  data class AddItem(
    val source: String,
  ) : CategoryNavigation

  data object Calendar : CategoryNavigation

  data object Edit : CategoryNavigation

  data class ItemActions(
    val itemIds: List<String>,
  ) : CategoryNavigation
}

sealed interface CategoryNavigationSideEffect {
  data object OpenAdd : CategoryNavigationSideEffect

  data object OpenCalendar : CategoryNavigationSideEffect

  data object OpenEditor : CategoryNavigationSideEffect

  data class OpenItemActions(
    val itemIds: List<String>,
  ) : CategoryNavigationSideEffect

  /** The category was deleted: leave the screen. */
  data object Close : CategoryNavigationSideEffect
}

sealed interface CategorySideEffect {
  data class ShowDeleteConfirm(
    val name: String,
    val productCount: Int,
  ) : CategorySideEffect

  data object DeleteFailed : CategorySideEffect

  data class ShowSnackbar(
    val request: SnackbarRequest,
  ) : CategorySideEffect
}
