package com.alorma.caducity.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.HomeRow
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveHomeUseCase
import com.alorma.caducity.domain.usecase.ObserveWeekStripUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemActionAction
import com.alorma.caducity.feature.tracking.NavigateToAddFromHomeAction
import com.alorma.caducity.feature.tracking.NavigateToCalendarFromHomeAction
import com.alorma.caducity.feature.tracking.NavigateToItemActionsFromHomeAction
import com.alorma.caducity.feature.tracking.NavigateToPantryFromHomeAction
import com.alorma.caducity.feature.tracking.NavigateToSearchAction
import com.alorma.caducity.feature.tracking.NavigateToStatusListFromHomeAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbar
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
  observeHome: ObserveHomeUseCase,
  observeWeekStrip: ObserveWeekStripUseCase,
  private val consumeItem: ConsumeItemUseCase,
  private val freezeItem: FreezeItemUseCase,
  private val restoreItems: RestoreItemsUseCase,
  private val snackbars: GlobalSnackbarEvents,
  private val eventTracker: EventTracker,
) : BaseViewModel<HomeNavigation, HomeNavigationSideEffect, Unit>() {
  val state: StateFlow<HomeState> =
    combine(observeHome.observe(), observeWeekStrip.observe()) { data, weekStrip ->
      HomeState.Ready(data = data, weekStrip = weekStrip) as HomeState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState.Loading)

  override fun navigate(navigation: HomeNavigation) {
    when (navigation) {
      is HomeNavigation.Calendar -> {
        eventTracker.trackAction(NavigateToCalendarFromHomeAction(navigation.source))
        emitNavigationSideEffect(HomeNavigationSideEffect.OpenCalendar(navigation.date))
      }
      HomeNavigation.Search -> {
        eventTracker.trackAction(NavigateToSearchAction())
        emitNavigationSideEffect(HomeNavigationSideEffect.OpenSearch)
      }
      is HomeNavigation.StatusList -> {
        eventTracker.trackAction(
          NavigateToStatusListFromHomeAction(navigation.status.name.lowercase(), navigation.source),
        )
        emitNavigationSideEffect(HomeNavigationSideEffect.OpenStatusList(navigation.status))
      }
      is HomeNavigation.Pantry -> {
        eventTracker.trackAction(NavigateToPantryFromHomeAction(navigation.source))
        emitNavigationSideEffect(HomeNavigationSideEffect.OpenPantry)
      }
      is HomeNavigation.ItemActions -> {
        eventTracker.trackAction(NavigateToItemActionsFromHomeAction())
        emitNavigationSideEffect(HomeNavigationSideEffect.OpenItemActions(navigation.itemId))
      }
      HomeNavigation.AddFirst -> {
        eventTracker.trackAction(NavigateToAddFromHomeAction())
        emitNavigationSideEffect(HomeNavigationSideEffect.OpenAdd)
      }
    }
  }

  /** ✓ marks **one** unit (the closest to expire) of the row as consumed (P6), with undo. */
  fun onConsume(row: HomeRow) {
    eventTracker.trackAction(ItemActionAction("consume_home"))
    viewModelScope.launch {
      consumeItem
        .consume(row.nearestItemId)
        .onSuccess {
          snackbars.show(
            GlobalSnackbar(
              message = R.string.item_actions_snackbar_consumed,
              messageArgs = listOf(row.name),
              actionLabel = R.string.add_undo,
              type = AppFeedbackType.Success,
              onAction = { restoreItems.restore(listOf(row.nearestItemId)) },
            ),
          )
        }.onFailure {
          snackbars.show(GlobalSnackbar(R.string.item_actions_error, type = AppFeedbackType.Error))
        }
    }
  }

  /** ❄ freezes **one** unit (the closest to expire) of the row, with undo. */
  fun onFreeze(row: HomeRow) {
    eventTracker.trackAction(ItemActionAction("freeze_home"))
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(listOf(row.nearestItemId))
      freezeItem
        .freeze(listOf(row.nearestItemId))
        .onSuccess {
          snackbars.show(
            GlobalSnackbar(
              message = R.string.item_actions_snackbar_frozen,
              messageArgs = listOf(row.name),
              actionLabel = R.string.add_undo,
              type = AppFeedbackType.Success,
              onAction = { restoreItems.restoreSnapshots(snapshots) },
            ),
          )
        }.onFailure {
          snackbars.show(GlobalSnackbar(R.string.item_actions_error, type = AppFeedbackType.Error))
        }
    }
  }
}
