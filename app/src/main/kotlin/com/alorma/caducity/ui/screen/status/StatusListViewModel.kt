package com.alorma.caducity.ui.screen.status

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.StatusListData
import com.alorma.caducity.domain.model.sortedNearestFirst
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsByStatusUseCase
import com.alorma.caducity.domain.usecase.RescheduleItemUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemActionAction
import com.alorma.caducity.feature.tracking.NavigateToAddFromStatusListAction
import com.alorma.caducity.feature.tracking.NavigateToItemActionsFromStatusListAction
import com.alorma.caducity.feature.tracking.NavigateToTransformAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbar
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * One list for the four statuses (Caducats, Aviat, Frescos, Congelats). The round buttons of a row act on
 * **one** unit (the closest to expire); the bulk actions of the selection act on every unit of the rows.
 */
class StatusListViewModel(
  private val status: ExpiryStatus,
  observeByStatus: ObserveItemsByStatusUseCase,
  private val consumeItem: ConsumeItemUseCase,
  private val freezeItem: FreezeItemUseCase,
  private val rescheduleItem: RescheduleItemUseCase,
  private val discardItems: DiscardItemsUseCase,
  private val restoreItems: RestoreItemsUseCase,
  private val snackbars: GlobalSnackbarEvents,
  private val eventTracker: EventTracker,
) : BaseViewModel<StatusListNavigation, StatusListNavigationSideEffect, StatusListSideEffect>() {
  private val selecting = MutableStateFlow(false)
  private val selectedKeys = MutableStateFlow(emptySet<String>())

  val state: StateFlow<StatusListState> =
    combine<_, _, _, StatusListState>(
      observeByStatus.observe(status),
      selecting,
      selectedKeys,
    ) { data, isSelecting, selected ->
      val sections = data.toSections()
      val keys = sections.flatMap { s -> s.rows.map { it.key } }.toSet()
      StatusListState.Ready(
        status = status,
        totalCount = data.totalCount,
        sections = sections,
        selecting = isSelecting,
        selectedKeys = selected intersect keys,
      )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatusListState.Loading)

  override fun navigate(navigation: StatusListNavigation) {
    when (navigation) {
      is StatusListNavigation.ItemActions -> {
        eventTracker.trackAction(NavigateToItemActionsFromStatusListAction(status.name.lowercase()))
        emitNavigationSideEffect(StatusListNavigationSideEffect.OpenItemActions(navigation.itemId))
      }
      StatusListNavigation.AddFirst -> {
        eventTracker.trackAction(NavigateToAddFromStatusListAction(status.name.lowercase()))
        emitNavigationSideEffect(StatusListNavigationSideEffect.OpenAdd)
      }
      StatusListNavigation.TransformSelection ->
        withSelection { rows ->
          eventTracker.trackAction(NavigateToTransformAction(origin = "status_list", source = "selection"))
          emitNavigationSideEffect(StatusListNavigationSideEffect.OpenTransform(rows.flatMap { it.itemIds }))
          onToggleSelecting()
        }
    }
  }

  // region Selection

  fun onToggleSelecting() {
    selecting.value = !selecting.value
    selectedKeys.value = emptySet()
  }

  fun onToggleRow(key: String) {
    selectedKeys.value = selectedKeys.value.let { if (key in it) it - key else it + key }
  }

  // endregion

  // region One unit (the row's round buttons)

  fun onConsume(row: StatusRow) = consume(listOf(row.nearest.itemId), row.name)

  fun onFreeze(row: StatusRow) = freeze(listOf(row.nearest.itemId), row.name)

  fun onDiscard(row: StatusRow) = discard(listOf(row.nearest.itemId), row.name)

  // endregion

  // region Bulk (selection and «Llença'ls tots»)

  fun onBulkConsume() = withSelection { rows -> consume(rows.flatMap { it.itemIds }, rows.singleName()) }

  fun onBulkFreeze() = withSelection { rows -> freeze(rows.flatMap { it.itemIds }, rows.singleName()) }

  fun onBulkRescheduleClick() =
    withSelection { rows ->
      emitSideEffect(
        StatusListSideEffect.ShowQuickDates(
          DateTarget(rows.flatMap { it.itemIds }, rows.map { it.name }),
        ),
      )
    }

  fun onBulkDiscardClick() =
    withSelection { rows ->
      val ids = rows.flatMap { it.itemIds }
      emitSideEffect(StatusListSideEffect.ShowDiscardConfirm(ids, ids.size))
    }

  fun onDiscardAllClick() {
    val ready = state.value as? StatusListState.Ready ?: return
    val ids = ready.rows.flatMap { it.itemIds }
    if (ids.isNotEmpty()) emitSideEffect(StatusListSideEffect.ShowDiscardConfirm(ids, ids.size))
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

  /** The quick-dates sheet (or the picker) gave a date: the selected units are rescheduled to it. */
  fun onDateChosen(
    target: DateTarget,
    date: LocalDate,
  ) {
    val ids = target.itemIds
    val name = target.names.singleOrNull().takeIf { ids.size == 1 }
    eventTracker.trackAction(ItemActionAction("reschedule_status"))
    undoable(
      ids = ids,
      snackbar = { undo ->
        if (name != null) {
          GlobalSnackbar(
            message = R.string.item_actions_snackbar_rescheduled,
            messageArgs = listOf(name),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Success,
            onAction = undo,
          )
        } else {
          GlobalSnackbar(
            message = R.string.status_snackbar_rescheduled_many,
            messageArgs = listOf(ids.size),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Success,
            onAction = undo,
          )
        }
      },
    ) { rescheduleItem.reschedule(ids, date) }
  }

  private fun consume(
    ids: List<String>,
    name: String?,
  ) {
    eventTracker.trackAction(ItemActionAction("consume_status"))
    undoable(
      ids = ids,
      snackbar = { undo ->
        if (ids.size == 1 && name != null) {
          GlobalSnackbar(
            message = R.string.item_actions_snackbar_consumed,
            messageArgs = listOf(name),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Success,
            onAction = undo,
          )
        } else {
          GlobalSnackbar(
            message = R.string.status_snackbar_consumed_many,
            messageArgs = listOf(ids.size),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Success,
            onAction = undo,
          )
        }
      },
    ) { consumeItem.consume(ids) }
  }

  private fun freeze(
    ids: List<String>,
    name: String?,
  ) {
    eventTracker.trackAction(ItemActionAction("freeze_status"))
    undoable(
      ids = ids,
      snackbar = { undo ->
        if (ids.size == 1 && name != null) {
          GlobalSnackbar(
            message = R.string.item_actions_snackbar_frozen,
            messageArgs = listOf(name),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Success,
            onAction = undo,
          )
        } else {
          GlobalSnackbar(
            message = R.string.status_snackbar_frozen_many,
            messageArgs = listOf(ids.size),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Success,
            onAction = undo,
          )
        }
      },
    ) { freezeItem.freeze(ids) }
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
          GlobalSnackbar(
            message = R.string.status_snackbar_discarded,
            messageArgs = listOf(name),
            actionLabel = R.string.add_undo,
            type = AppFeedbackType.Info,
            onAction = undo,
          )
        } else {
          GlobalSnackbar(
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
    snackbar: (undo: suspend () -> Unit) -> GlobalSnackbar,
    action: suspend () -> Result<*>,
  ) {
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(ids)
      action()
        .onSuccess {
          selectedKeys.value = emptySet()
          snackbars.show(snackbar { restoreItems.restoreSnapshots(snapshots) })
        }.onFailure {
          snackbars.show(GlobalSnackbar(R.string.item_actions_error, type = AppFeedbackType.Error))
        }
    }
  }

  private fun withSelection(block: (List<StatusRow>) -> Unit) {
    val ready = state.value as? StatusListState.Ready ?: return
    val rows = ready.selectedRows
    if (rows.isNotEmpty()) block(rows)
  }

  private fun List<StatusRow>.singleName(): String? = singleOrNull()?.takeIf { it.unitCount == 1 }?.name

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
