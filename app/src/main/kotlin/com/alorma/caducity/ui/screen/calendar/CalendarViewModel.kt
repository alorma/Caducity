package com.alorma.caducity.ui.screen.calendar

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.calendar.FirstDayOfWeekPreferences
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.unitPositions
import com.alorma.caducity.domain.usecase.ObserveCalendarUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.feature.tracking.CalendarDaySelectAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToItemActionsFromCalendarAction
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Month view: the grid with a dot per unit, and the units of the selected day. */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
  private val args: CalendarArgs,
  private val observeCalendar: ObserveCalendarUseCase,
  observeItems: ObserveItemsUseCase,
  observeCategories: ObserveCategoriesUseCase,
  firstDayOfWeekPreferences: FirstDayOfWeekPreferences,
  private val todayProvider: TodayProvider,
  private val eventTracker: EventTracker,
) : BaseViewModel<CalendarNavigation, CalendarNavigationSideEffect, Unit>() {
  private val initialSelected = args.date ?: todayProvider.today()
  private val monthStart = MutableStateFlow(initialSelected.firstOfMonth())
  private val selected = MutableStateFlow(initialSelected)

  private data class Params(
    val monthStart: LocalDate,
    val selected: LocalDate,
    val firstDayOfWeek: DayOfWeek,
    val today: LocalDate,
  )

  val state: StateFlow<CalendarState> =
    combine(
      monthStart,
      selected,
      firstDayOfWeekPreferences.state.map { it.firstDayOfWeek },
      todayProvider.observe(),
      ::Params,
    ).flatMapLatest { params ->
      val month = CalendarMonthModel.of(params.monthStart, params.firstDayOfWeek)
      combine(
        // The neighbour months are loaded too, so the pager shows their dots while it is being dragged.
        observeCalendar.observe(month.previous().first..month.next().last, args.categoryId),
        observeItems.observe(),
        observeCategories.observe(),
      ) { byDate, items, categories ->
        val categoriesById = categories.associateBy { it.id }
        val positions = items.unitPositions()
        CalendarState.Ready(
          month = month,
          today = params.today,
          selected = params.selected,
          categoryName = args.categoryId?.let { categoriesById[it]?.name },
          statusesByDate = byDate.mapValues { (_, units) -> units.map { it.status }.sortedBy { it.ordinal } },
          rows =
            byDate[params.selected].orEmpty().mapNotNull { entry ->
              val category = categoriesById[entry.categoryId] ?: return@mapNotNull null
              CalendarRow(
                itemId = entry.itemId,
                name = entry.name,
                category = category,
                position = positions.getValue(entry.itemId),
                status = entry.status,
                daysLeft = params.today.daysUntil(entry.expiration),
              )
            },
        ) as CalendarState
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarState.Loading)

  override fun navigate(navigation: CalendarNavigation) {
    when (navigation) {
      CalendarNavigation.Back -> emitNavigationSideEffect(CalendarNavigationSideEffect.Back)
      is CalendarNavigation.ItemActions -> {
        eventTracker.trackAction(NavigateToItemActionsFromCalendarAction())
        emitNavigationSideEffect(CalendarNavigationSideEffect.OpenItemActions(navigation.itemId))
      }
    }
  }

  fun onDayClick(date: LocalDate) {
    eventTracker.trackAction(CalendarDaySelectAction())
    selected.value = date
  }

  /** The pager settled on the month starting at [first]; it selects today if it is in that month, otherwise its first day. */
  fun onMonthShown(first: LocalDate) {
    if (first == monthStart.value) return
    monthStart.value = first
    val today = todayProvider.today()
    selected.value = if (today.firstOfMonth() == first) today else first
  }

  private fun LocalDate.firstOfMonth() = LocalDate(year, month, 1)
}
