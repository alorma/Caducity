package com.alorma.caducity.ui.screen.calendar

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.calendar.FirstDayOfWeekPreferences
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.ProductRowDetail
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.model.toProductRow
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveCalendarUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.feature.tracking.CalendarDaySelectAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemActionAction
import com.alorma.caducity.feature.tracking.NavigateToItemActionsFromCalendarAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.SnackbarRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** Month view: the grid with a dot per unit, and the products of the selected day. */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
  private val args: CalendarArgs,
  private val observeCalendar: ObserveCalendarUseCase,
  observeCategories: ObserveCategoriesUseCase,
  firstDayOfWeekPreferences: FirstDayOfWeekPreferences,
  private val todayProvider: TodayProvider,
  private val eventTracker: EventTracker,
  private val consumeItem: ConsumeItemUseCase,
  private val freezeItem: FreezeItemUseCase,
  private val discardItems: DiscardItemsUseCase,
  private val restoreItems: RestoreItemsUseCase,
) : BaseViewModel<CalendarNavigation, CalendarNavigationSideEffect, CalendarSideEffect>() {
  private fun showSnackbar(request: SnackbarRequest) = emitSideEffect(CalendarSideEffect.ShowSnackbar(request))

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
        observeCategories.observe(),
      ) { byDate, categories ->
        val categoriesById = categories.associateBy { it.id }
        CalendarState.Ready(
          month = month,
          today = params.today,
          selected = params.selected,
          categoryName = args.categoryId?.let { categoriesById[it]?.name },
          // One status per product: the cell's dots and description count products, not units.
          statusesByDate =
            byDate.mapValues { (_, units) ->
              units.distinctBy { it.groupKey }.map { it.status }.sortedBy { it.ordinal }
            },
          rows =
            byDate[params.selected]
              .orEmpty()
              .filter { categoriesById.containsKey(it.categoryId) }
              .groupBy { it.groupKey }
              .values
              .map { units ->
                // The icon only tells the categories apart when the calendar is not filtered by one.
                val category = categoriesById.getValue(units.first().categoryId)
                units.toProductRow(
                  category = category.takeIf { args.categoryId == null },
                  detail = ProductRowDetail.WithDays,
                  today = params.today,
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
        emitNavigationSideEffect(CalendarNavigationSideEffect.OpenItemActions(navigation.itemIds))
      }
    }
  }

  fun onDayClick(date: LocalDate) {
    eventTracker.trackAction(CalendarDaySelectAction())
    selected.value = date
  }

  // The round buttons of a row act on **one** unit: the one that expires first (like Home).

  fun onConsume(row: ProductRowModel) {
    actOnNearest(row, "consume_calendar", R.string.item_actions_snackbar_consumed, AppFeedbackType.Success) {
      consumeItem.consume(it)
    }
  }

  fun onFreeze(row: ProductRowModel) {
    actOnNearest(row, "freeze_calendar", R.string.item_actions_snackbar_frozen, AppFeedbackType.Success) {
      freezeItem.freeze(it)
    }
  }

  fun onDiscard(row: ProductRowModel) {
    actOnNearest(row, "discard_calendar", R.string.status_snackbar_discarded, AppFeedbackType.Info) {
      discardItems.discard(it)
    }
  }

  /** Performs [perform] on the nearest unit of [row] and, if it worked, shows the snackbar whose «Desfés» undoes it. */
  private fun actOnNearest(
    row: ProductRowModel,
    trackingName: String,
    message: Int,
    type: AppFeedbackType,
    perform: suspend (List<String>) -> Result<*>,
  ) {
    eventTracker.trackAction(ItemActionAction(trackingName))
    val ids = listOf(row.nearestItemId)
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(ids)
      perform(ids)
        .onSuccess {
          showSnackbar(
            SnackbarRequest(
              message = message,
              messageArgs = listOf(row.name),
              actionLabel = R.string.add_undo,
              type = type,
              onAction = { restoreItems.restoreSnapshots(snapshots) },
            ),
          )
        }.onFailure {
          showSnackbar(SnackbarRequest(R.string.item_actions_error, type = AppFeedbackType.Error))
        }
    }
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
