package com.alorma.caducity.ui.components.actions

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.unitPositions
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.MoveItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.RescheduleItemUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.domain.usecase.toExpirationInstant
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemActionAction
import com.alorma.caducity.feature.tracking.NavigateToEditItemAction
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/** Actions on **one unit** (P2): consume, freeze, change the date, edit, move or delete it. */
class ItemActionsSheetViewModel(
  private val itemId: String,
  observeItems: ObserveItemsUseCase,
  observeCategories: ObserveCategoriesUseCase,
  private val consumeItem: ConsumeItemUseCase,
  private val freezeItem: FreezeItemUseCase,
  private val unfreezeItem: UnfreezeItemUseCase,
  private val rescheduleItem: RescheduleItemUseCase,
  private val deleteItem: DeleteItemUseCase,
  private val moveItem: MoveItemToCategoryUseCase,
  private val restoreItems: RestoreItemsUseCase,
  private val appClock: AppClock,
  private val snackbars: GlobalSnackbarEvents,
  private val eventTracker: EventTracker,
) : BaseViewModel<ItemActionsSheetNavigation, ItemActionsSheetNavigationSideEffect, ItemActionsSheetSideEffect>() {
  private val pickingCategory = MutableStateFlow(false)
  private var closing = false

  val state: StateFlow<ItemActionsSheetState> =
    combine(observeItems.observe(), observeCategories.observe(), pickingCategory) { entries, categories, picking ->
      val entry = entries.firstOrNull { it.itemId == itemId }
      val category = entry?.let { e -> categories.firstOrNull { it.id == e.categoryId } }
      if (entry == null || category == null) {
        // The unit is gone (acted on, or removed elsewhere): nothing left to act on.
        if (entries.isNotEmpty() || categories.isNotEmpty()) close()
        ItemActionsSheetState.Loading
      } else {
        val position = entries.unitPositions().getValue(itemId)
        ItemActionsSheetState.Ready(
          name = entry.name,
          category = category,
          unitIndex = position.index,
          unitCount = position.count,
          expiration = entry.expiration,
          status = entry.status,
          daysLeft = appClock.nowDate().daysUntil(entry.expiration),
          otherCategories = categories.filter { it.id != category.id },
          pickingCategory = picking,
        )
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemActionsSheetState.Loading)

  override fun navigate(navigation: ItemActionsSheetNavigation) {
    when (navigation) {
      ItemActionsSheetNavigation.Close -> close()
      ItemActionsSheetNavigation.Edit -> {
        eventTracker.trackAction(NavigateToEditItemAction())
        closing = true
        emitNavigationSideEffect(ItemActionsSheetNavigationSideEffect.Edit(itemId))
      }
      ItemActionsSheetNavigation.Transform -> {
        eventTracker.trackAction(NavigateToTransformAction(origin = "item_actions", source = "item_actions"))
        closing = true
        emitNavigationSideEffect(ItemActionsSheetNavigationSideEffect.Transform(itemId))
      }
    }
  }

  fun onConsume() {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("consume"))
    viewModelScope.launch {
      consumeItem
        .consume(itemId)
        .onSuccess {
          feedback(
            GlobalSnackbar(
              message = R.string.item_actions_snackbar_consumed,
              messageArgs = listOf(ready.name),
              actionLabel = R.string.add_undo,
              type = AppFeedbackType.Success,
              onAction = { restoreItems.restore(listOf(itemId)) },
            ),
          )
        }.onFailure { failed() }
    }
  }

  fun onFreezeToggle() {
    val ready = ready() ?: return
    if (ready.frozen) {
      eventTracker.trackAction(ItemActionAction("unfreeze"))
      viewModelScope.launch {
        val proposed = unfreezeItem.proposedDate(itemId) ?: ready.expiration
        emitSideEffect(ItemActionsSheetSideEffect.ShowDatePicker(proposed.toUtcMillis(), unfreeze = true))
      }
      return
    }
    eventTracker.trackAction(ItemActionAction("freeze"))
    viewModelScope.launch {
      freezeItem
        .freezeItem(itemId, ready.expiration.toExpirationInstant())
        .onSuccess {
          feedback(
            GlobalSnackbar(
              message = R.string.item_actions_snackbar_frozen,
              messageArgs = listOf(ready.name),
              actionLabel = R.string.add_undo,
              type = AppFeedbackType.Status(ItemStatus.Frozen),
              onAction = { freezeItem.unfreezeItem(itemId) },
            ),
          )
        }.onFailure {
          if (it is InstanceActionError.CannotFreezeExpiredInstance) {
            snackbars.show(
              GlobalSnackbar(R.string.item_actions_error_freeze_expired, type = AppFeedbackType.Error),
            )
          } else {
            snackbars.show(GlobalSnackbar(R.string.item_actions_error, type = AppFeedbackType.Error))
          }
        }
    }
  }

  fun onQuickReschedule(option: QuickReschedule) {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("quick_date"))
    reschedule(ready.expiration.plus(option.days, DateTimeUnit.DAY))
  }

  fun onPickDate() {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("pick_date"))
    emitSideEffect(ItemActionsSheetSideEffect.ShowDatePicker(ready.expiration.toUtcMillis(), unfreeze = false))
  }

  /** The date chosen in the picker: reschedules, or (when the unit is frozen) unfreezes with that date. */
  fun onDatePicked(date: LocalDate) {
    val ready = ready() ?: return
    if (!ready.frozen) {
      reschedule(date)
      return
    }
    viewModelScope.launch {
      unfreezeItem
        .unfreezeOn(itemId, date)
        .onSuccess {
          feedback(
            GlobalSnackbar(
              message = R.string.item_actions_snackbar_unfrozen,
              messageArgs = listOf(ready.name),
              type = AppFeedbackType.Success,
            ),
          )
        }.onFailure { failed() }
    }
  }

  fun onDelete() {
    val ready = ready() ?: return
    emitSideEffect(ItemActionsSheetSideEffect.ShowDeleteConfirm(ready.name))
  }

  fun onDeleteConfirmed() {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("delete"))
    viewModelScope.launch {
      deleteItem
        .deleteItem(itemId)
        .onSuccess {
          feedback(
            GlobalSnackbar(
              message = R.string.item_actions_snackbar_deleted,
              messageArgs = listOf(ready.name),
              type = AppFeedbackType.Info,
            ),
          )
        }.onFailure { failed() }
    }
  }

  fun onMoveClick() {
    pickingCategory.value = true
  }

  fun onMoveBack() {
    pickingCategory.value = false
  }

  fun onMove(categoryId: String) {
    val ready = ready() ?: return
    val target = ready.otherCategories.firstOrNull { it.id == categoryId } ?: return
    eventTracker.trackAction(ItemActionAction("move"))
    viewModelScope.launch {
      moveItem
        .move(itemId, categoryId)
        .onSuccess {
          feedback(
            GlobalSnackbar(
              message = R.string.item_actions_snackbar_moved,
              messageArgs = listOf(ready.name, target.name),
              actionLabel = R.string.add_undo,
              onAction = { moveItem.move(itemId, ready.category.id) },
            ),
          )
        }.onFailure { failed() }
    }
  }

  private fun reschedule(date: LocalDate) {
    val ready = ready() ?: return
    val previous = ready.expiration
    viewModelScope.launch {
      rescheduleItem
        .rescheduleItem(itemId, date.toExpirationInstant())
        .onSuccess {
          feedback(
            GlobalSnackbar(
              message = R.string.item_actions_snackbar_rescheduled,
              messageArgs = listOf(ready.name),
              actionLabel = R.string.add_undo,
              onAction = { rescheduleItem.rescheduleItem(itemId, previous.toExpirationInstant()) },
            ),
          )
        }.onFailure { failed() }
    }
  }

  private fun feedback(snackbar: GlobalSnackbar) {
    emitSideEffect(ItemActionsSheetSideEffect.Confirm)
    snackbars.show(snackbar)
    close()
  }

  private fun failed() {
    snackbars.show(GlobalSnackbar(R.string.item_actions_error, type = AppFeedbackType.Error))
  }

  private fun close() {
    if (closing) return
    closing = true
    emitNavigationSideEffect(ItemActionsSheetNavigationSideEffect.Close)
  }

  private fun ready() = state.value as? ItemActionsSheetState.Ready

  private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}
