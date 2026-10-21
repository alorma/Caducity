package com.alorma.caducity.ui.screen.status

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.StatusListData
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.sortedNearestFirst
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsByStatusUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemActionAction
import com.alorma.caducity.feature.tracking.NavigateToAddFromStatusListAction
import com.alorma.caducity.feature.tracking.NavigateToItemActionsFromStatusListAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.actions.lowerWithSnackbar
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.SnackbarRequest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * One list for the four statuses (Caducats, Aviat, Frescos, Congelats). The round buttons of a row act on
 * **one** unit (the closest to expire); «Llença'ls tots» acts on every unit of the list.
 */
class StatusListViewModel(
  private val status: ExpiryStatus,
  observeByStatus: ObserveItemsByStatusUseCase,
  expirationThresholds: ExpirationThresholds,
  private val consumeItem: ConsumeItemUseCase,
  private val freezeItem: FreezeItemUseCase,
  private val unfreezeItem: UnfreezeItemUseCase,
  private val discardItems: DiscardItemsUseCase,
  private val restoreItems: RestoreItemsUseCase,
  private val eventTracker: EventTracker,
) : BaseViewModel<StatusListNavigation, StatusListNavigationSideEffect, StatusListSideEffect>() {
  private fun showSnackbar(request: SnackbarRequest) = emitSideEffect(StatusListSideEffect.ShowSnackbar(request))

  val state: StateFlow<StatusListState> =
    combine(observeByStatus.observe(status), expirationThresholds.soonThresholdDays) { data, soonDays ->
      StatusListState.Ready(
        status = status,
        totalCount = data.totalCount,
        sections = data.toSections(),
        soonDays = soonDays,
      )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatusListState.Loading)

  override fun navigate(navigation: StatusListNavigation) {
    when (navigation) {
      is StatusListNavigation.ItemActions -> {
        eventTracker.trackAction(NavigateToItemActionsFromStatusListAction(status.name.lowercase()))
        emitNavigationSideEffect(StatusListNavigationSideEffect.OpenItemActions(navigation.itemIds))
      }
      StatusListNavigation.AddFirst -> {
        eventTracker.trackAction(NavigateToAddFromStatusListAction(status.name.lowercase()))
        emitNavigationSideEffect(StatusListNavigationSideEffect.OpenAdd)
      }
    }
  }

  // region One unit (the row's round buttons)

  fun onConsume(row: StatusRow) {
    val nearest = row.nearest
    if (nearest.unitType.parent == UnitParent.HANDFULS) {
      eventTracker.trackAction(ItemActionAction("lower_level_status"))
      viewModelScope.launch {
        consumeItem
          .lowerWithSnackbar(nearest.itemId, row.name, nearest.level, restoreItems)
          .onSuccess { showSnackbar(it) }
          .onFailure { showSnackbar(SnackbarRequest(R.string.item_actions_error, type = AppFeedbackType.Error)) }
      }
    } else {
      consume(nearest.itemId, row.name)
    }
  }

  fun onFreeze(row: StatusRow) = freeze(row.nearest.itemId, row.name)

  fun onUnfreeze(row: StatusRow) = unfreeze(row.nearest.itemId, row.name)

  fun onDiscard(row: StatusRow) = discard(listOf(row.nearest.itemId), row.name)

  // endregion

  // region «Llença'ls tots»

  fun onDiscardAllClick() {
    val ready = state.value as? StatusListState.Ready ?: return
    val ids = ready.rows.flatMap { it.itemIds }
    if (ids.isNotEmpty()) emitSideEffect(StatusListSideEffect.ShowDiscardConfirm(ids, ready.totalCount))
  }

  fun onDiscardConfirmed(itemIds: List<String>) {
    val name =
      (state.value as? StatusListState.Ready)
        ?.rows
        ?.firstOrNull { itemIds.singleOrNull() == it.nearest.itemId }
        ?.name
    discard(itemIds, name)
  }

  // endregion

  private fun consume(
    id: String,
    name: String,
  ) {
    eventTracker.trackAction(ItemActionAction("consume_status"))
    undoable(
      ids = listOf(id),
      snackbar = { undo ->
        SnackbarRequest(
          message = R.string.item_actions_snackbar_consumed,
          messageArgs = listOf(name),
          actionLabel = R.string.add_undo,
          type = AppFeedbackType.Success,
          onAction = undo,
        )
      },
    ) { consumeItem.consume(listOf(id)) }
  }

  private fun freeze(
    id: String,
    name: String,
  ) {
    eventTracker.trackAction(ItemActionAction("freeze_status"))
    undoable(
      ids = listOf(id),
      snackbar = { undo ->
        SnackbarRequest(
          message = R.string.item_actions_snackbar_frozen,
          messageArgs = listOf(name),
          actionLabel = R.string.add_undo,
          type = AppFeedbackType.Success,
          onAction = undo,
        )
      },
    ) { freezeItem.freeze(listOf(id)) }
  }

  private fun unfreeze(
    id: String,
    name: String,
  ) {
    eventTracker.trackAction(ItemActionAction("unfreeze_status"))
    undoable(
      ids = listOf(id),
      snackbar = { undo ->
        SnackbarRequest(
          message = R.string.snack_unfrozen,
          messageArgs = listOf(1, name),
          actionLabel = R.string.add_undo,
          type = AppFeedbackType.Success,
          onAction = undo,
        )
      },
    ) { unfreezeItem.unfreeze(listOf(id)) }
  }

  private fun discard(
    ids: List<String>,
    name: String?,
  ) {
    eventTracker.trackAction(ItemActionAction("discard_status"))
    undoable(
      ids = ids,
      snackbar = { undo ->
        if (ids.size == 1 && name != null) {
          SnackbarRequest(
            message = R.string.status_snackbar_discarded,
            messageArgs = listOf(name),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Info,
            onAction = undo,
          )
        } else {
          SnackbarRequest(
            message = R.string.status_snackbar_discarded_many,
            messageArgs = listOf(ids.size),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Info,
            onAction = undo,
          )
        }
      },
    ) { discardItems.discard(ids) }
  }

  /** Runs [action] and, if it worked, shows the snackbar whose «Desfés» puts every unit back exactly as it was. */
  private fun undoable(
    ids: List<String>,
    snackbar: (undo: suspend () -> Unit) -> SnackbarRequest,
    action: suspend () -> Result<*>,
  ) {
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(ids)
      action()
        .onSuccess {
          showSnackbar(snackbar { restoreItems.restoreSnapshots(snapshots) })
        }.onFailure {
          showSnackbar(SnackbarRequest(R.string.item_actions_error, type = AppFeedbackType.Error))
        }
    }
  }

  private fun StatusListData.toSections(): List<StatusSection> =
    groups.map { group ->
      val rows =
        group.entries
          .groupBy { entry ->
            val day = if (status == ExpiryStatus.FROZEN) entry.frozenSince?.date() else entry.expiration
            "${entry.groupKey}|$day"
          }.map { (key, units) ->
            val first = units.first()
            StatusRow(
              key = key,
              name = first.name,
              category = group.category,
              expiration = first.expiration,
              frozenSince = first.frozenSince?.date(),
              entries = units.sortedNearestFirst(),
            )
          }
      StatusSection(category = group.category, rows = rows)
    }
}
