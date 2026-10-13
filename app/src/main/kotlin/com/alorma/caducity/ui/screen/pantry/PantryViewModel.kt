package com.alorma.caducity.ui.screen.pantry

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObservePantryUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemActionAction
import com.alorma.caducity.feature.tracking.NavigateToCategoryFromPantryAction
import com.alorma.caducity.feature.tracking.NavigateToNewCategoryFromPantryAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbar
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PantryViewModel(
  private val observePantry: ObservePantryUseCase,
  private val eventTracker: EventTracker,
  private val consumeItem: ConsumeItemUseCase,
  private val freezeItem: FreezeItemUseCase,
  private val discardItems: DiscardItemsUseCase,
  private val restoreItems: RestoreItemsUseCase,
  private val snackbars: GlobalSnackbarEvents,
) : BaseViewModel<PantryNavigation, PantryNavigationSideEffect, Unit>() {
  private val query = MutableStateFlow("")

  @OptIn(ExperimentalCoroutinesApi::class)
  private val results =
    query.flatMapLatest { text ->
      if (text.isBlank()) flowOf(emptyList()) else observePantry.search(text)
    }

  val state: StateFlow<PantryState> =
    combine(observePantry.observe(), query, results) { data, text, found ->
      PantryState.Ready(data = data, query = text, results = found) as PantryState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PantryState.Loading)

  fun onQueryChange(text: String) {
    query.value = text
  }

  // The round buttons of a result act on **one** unit: the one that expires first (like Home).

  fun onConsume(row: ProductRowModel) {
    actOnNearest(row, "consume_pantry", R.string.item_actions_snackbar_consumed, AppFeedbackType.Success) {
      consumeItem.consume(it)
    }
  }

  fun onFreeze(row: ProductRowModel) {
    actOnNearest(row, "freeze_pantry", R.string.item_actions_snackbar_frozen, AppFeedbackType.Success) {
      freezeItem.freeze(it)
    }
  }

  fun onDiscard(row: ProductRowModel) {
    actOnNearest(row, "discard_pantry", R.string.status_snackbar_discarded, AppFeedbackType.Info) {
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
          snackbars.show(
            GlobalSnackbar(
              message = message,
              messageArgs = listOf(row.name),
              actionLabel = R.string.add_undo,
              type = type,
              onAction = { restoreItems.restoreSnapshots(snapshots) },
            ),
          )
        }.onFailure {
          snackbars.show(GlobalSnackbar(R.string.item_actions_error, type = AppFeedbackType.Error))
        }
    }
  }

  override fun navigate(navigation: PantryNavigation) {
    when (navigation) {
      is PantryNavigation.Category -> {
        eventTracker.trackAction(NavigateToCategoryFromPantryAction(navigation.source))
        emitNavigationSideEffect(PantryNavigationSideEffect.OpenCategory(navigation.categoryId))
      }
      PantryNavigation.NewCategory -> {
        eventTracker.trackAction(NavigateToNewCategoryFromPantryAction())
        emitNavigationSideEffect(PantryNavigationSideEffect.OpenNewCategory)
      }
    }
  }
}
