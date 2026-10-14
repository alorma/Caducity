package com.alorma.caducity.ui.screen.calendar

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductRowModel
import kotlinx.datetime.LocalDate

/** Arguments of the `Calendar` route. */
data class CalendarArgs(
  val categoryId: String? = null,
  val date: LocalDate? = null,
)

sealed interface CalendarState {
  data object Loading : CalendarState

  data class Ready(
    val month: CalendarMonthModel,
    val today: LocalDate,
    val selected: LocalDate,
    /** Name of the category the calendar is filtered by, if any. */
    val categoryName: String?,
    /** Statuses of the units of each day (past days included, so expired ones show up). */
    val statusesByDate: Map<LocalDate, List<ExpiryStatus>>,
    /** Products of the [selected] day, one row each; the category icon only when the calendar is not filtered. */
    val rows: List<ProductRowModel>,
  ) : CalendarState
}

sealed interface CalendarNavigation {
  data object Back : CalendarNavigation

  data class ItemActions(
    val itemIds: List<String>,
  ) : CalendarNavigation
}

sealed interface CalendarNavigationSideEffect {
  data object Back : CalendarNavigationSideEffect

  data class OpenItemActions(
    val itemIds: List<String>,
  ) : CalendarNavigationSideEffect
}
