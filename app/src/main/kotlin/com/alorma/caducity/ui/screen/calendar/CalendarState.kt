package com.alorma.caducity.ui.screen.calendar

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.UnitPosition
import kotlinx.datetime.LocalDate

/** Arguments of the `Calendar` route. */
data class CalendarArgs(
  val categoryId: String? = null,
  val date: LocalDate? = null,
)

data class CalendarRow(
  val itemId: String,
  val name: String,
  val category: Category,
  val position: UnitPosition,
  val pack: Int?,
  val status: ExpiryStatus,
  /** Days until the unit expires; negative when it already has. */
  val daysLeft: Int,
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
    /** Units of the [selected] day. */
    val rows: List<CalendarRow>,
  ) : CalendarState
}

sealed interface CalendarNavigation {
  data object Back : CalendarNavigation

  data class ItemActions(
    val itemId: String,
  ) : CalendarNavigation
}

sealed interface CalendarNavigationSideEffect {
  data object Back : CalendarNavigationSideEffect

  data class OpenItemActions(
    val itemId: String,
  ) : CalendarNavigationSideEffect
}
