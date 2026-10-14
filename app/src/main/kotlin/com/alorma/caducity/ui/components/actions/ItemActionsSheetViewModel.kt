package com.alorma.caducity.ui.components.actions

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ItemSnapshot
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.sortedNearestFirst
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
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

/**
 * Actions on the units of a row ([itemIds]): with one unit they act on it; with several, on the first
 * [ItemActionsSheetState.Ready.selected] ones, the ones that expire first.
 */
class ItemActionsSheetViewModel(
  private val itemIds: List<String>,
  observeItems: ObserveItemsUseCase,
  observeCategories: ObserveCategoriesUseCase,
  private val consumeItem: ConsumeItemUseCase,
  private val freezeItem: FreezeItemUseCase,
  private val unfreezeItem: UnfreezeItemUseCase,
  private val rescheduleItem: RescheduleItemUseCase,
  private val discardItems: DiscardItemsUseCase,
  private val moveItem: MoveItemToCategoryUseCase,
  private val restoreItems: RestoreItemsUseCase,
  private val appClock: AppClock,
  private val snackbars: GlobalSnackbarEvents,
  private val eventTracker: EventTracker,
) : BaseViewModel<ItemActionsSheetNavigation, ItemActionsSheetNavigationSideEffect, ItemActionsSheetSideEffect>(),
  ItemActionsHandlers {
  private val pickingCategory = MutableStateFlow(false)
  private val moreOpen = MutableStateFlow(false)
  private val chosen = MutableStateFlow<Int?>(null)
  private var closing = false

  val state: StateFlow<ItemActionsSheetState> =
    combine(
      observeItems.observe(),
      observeCategories.observe(),
      pickingCategory,
      moreOpen,
      chosen,
    ) { entries, categories, picking, more, chosenCount ->
      val units = entries.filter { it.itemId in itemIds }.sortedNearestFirst()
      val focus = units.firstOrNull()
      val category = focus?.let { f -> categories.firstOrNull { it.id == f.categoryId } }
      if (focus == null || category == null) {
        // The units are gone (acted on, or removed elsewhere): nothing left to act on.
        if (entries.isNotEmpty() || categories.isNotEmpty()) close()
        ItemActionsSheetState.Loading
      } else {
        val actionUnits = units.map { ActionUnit(it.itemId, it.expiration, it.status) }
        val expiredCount = actionUnits.count { it.status == ExpiryStatus.EXPIRED }
        val expired = actionUnits.first().status == ExpiryStatus.EXPIRED
        val default = if (expired) expiredCount else 1
        ItemActionsSheetState.Ready(
          name = focus.name,
          category = category,
          units = actionUnits,
          selected = (chosenCount ?: default).coerceIn(1, actionUnits.size),
          moreOpen = more,
          daysLeft = appClock.nowDate().daysUntil(actionUnits.first().expiration),
          otherCategories = categories.filter { it.id != category.id },
          pickingCategory = picking,
        )
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemActionsSheetState.Loading)

  override fun navigate(navigation: ItemActionsSheetNavigation) {
    val ready = ready()
    when (navigation) {
      ItemActionsSheetNavigation.Close -> close()
      ItemActionsSheetNavigation.Edit -> {
        val nearest = ready?.units?.first()?.id ?: itemIds.first()
        eventTracker.trackAction(NavigateToEditItemAction())
        closing = true
        emitNavigationSideEffect(ItemActionsSheetNavigationSideEffect.Edit(nearest))
      }
      ItemActionsSheetNavigation.Transform -> {
        val ids = ready?.units?.map { it.id } ?: itemIds
        eventTracker.trackAction(NavigateToTransformAction(origin = "item_actions", source = "item_actions"))
        closing = true
        emitNavigationSideEffect(ItemActionsSheetNavigationSideEffect.Transform(ids))
      }
    }
  }

  override fun onEdit() = navigate(ItemActionsSheetNavigation.Edit)

  override fun onTransform() = navigate(ItemActionsSheetNavigation.Transform)

  override fun onSelect(count: Int) {
    chosen.value = count
  }

  override fun onToggleMore() {
    moreOpen.value = !moreOpen.value
  }

  override fun onConsume() {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("consume"))
    consume(ready, ready.units.take(ready.selected).map { it.id })
  }

  /** «Consumit igualment», once held for long enough (or confirmed with TalkBack). */
  override fun onEatAnyway() {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("eat_anyway"))
    consume(ready, ready.units.take(ready.selected).map { it.id })
  }

  override fun onEatAnywayRequested() {
    emitSideEffect(ItemActionsSheetSideEffect.ShowEatAnywayConfirm)
  }

  private fun consume(
    ready: ItemActionsSheetState.Ready,
    ids: List<String>,
  ) {
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(ids)
      consumeItem
        .consume(ids)
        .onSuccess {
          feedback(
            countedSnackbar(
              count = ids.size,
              one = R.string.item_actions_snackbar_consumed,
              many = R.string.item_actions_snackbar_consumed_n,
              name = ready.name,
              type = AppFeedbackType.Success,
              snapshots = snapshots,
            ),
          )
        }.onFailure { failed() }
    }
  }

  override fun onFreezeToggle() {
    val ready = ready() ?: return
    if (ready.frozen) {
      eventTracker.trackAction(ItemActionAction("unfreeze"))
      viewModelScope.launch {
        val proposed = unfreezeItem.proposedDate(ready.units.first().id) ?: ready.expiration
        emitSideEffect(ItemActionsSheetSideEffect.ShowDatePicker(proposed.toUtcMillis(), DatePickerPurpose.Unfreeze))
      }
      return
    }
    if (ready.freezableCount == 0) {
      snackbars.show(GlobalSnackbar(R.string.item_actions_error_freeze_expired, type = AppFeedbackType.Error))
      return
    }
    eventTracker.trackAction(ItemActionAction("freeze"))
    val ids =
      ready.units
        .filter { it.status == ExpiryStatus.SOON || it.status == ExpiryStatus.FRESH }
        .take(ready.freezeCount)
        .map { it.id }
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(ids)
      freezeItem
        .freeze(ids)
        .onSuccess { frozen ->
          feedback(
            countedSnackbar(
              count = frozen.size,
              one = R.string.item_actions_snackbar_frozen,
              many = R.string.item_actions_snackbar_frozen_n,
              name = ready.name,
              type = AppFeedbackType.Status(ItemStatus.Frozen),
              snapshots = snapshots,
            ),
          )
        }.onFailure { failed() }
    }
  }

  override fun onQuickReschedule(option: QuickReschedule) {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("quick_date"))
    val ids = ready.units.map { it.id }
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(ids)
      val failure =
        ready.units.firstNotNullOfOrNull { unit ->
          rescheduleItem
            .rescheduleItem(unit.id, unit.expiration.plus(option.days, DateTimeUnit.DAY).toExpirationInstant())
            .exceptionOrNull()
        }
      if (failure == null) feedback(rescheduledSnackbar(ready.name, snapshots)) else failed()
    }
  }

  override fun onPickDate() {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("pick_date"))
    emitSideEffect(
      ItemActionsSheetSideEffect.ShowDatePicker(ready.expiration.toUtcMillis(), DatePickerPurpose.Reschedule),
    )
  }

  /** «La data era incorrecta»: a new date for the expired units, which were typed wrong. */
  override fun onFixDate() {
    val ready = ready() ?: return
    eventTracker.trackAction(ItemActionAction("fix_date"))
    emitSideEffect(
      ItemActionsSheetSideEffect.ShowDatePicker(ready.expiration.toUtcMillis(), DatePickerPurpose.FixExpiredDate),
    )
  }

  /** The date chosen in the picker. */
  fun onDatePicked(
    date: LocalDate,
    purpose: DatePickerPurpose,
  ) {
    val ready = ready() ?: return
    val ids =
      when (purpose) {
        DatePickerPurpose.Reschedule, DatePickerPurpose.Unfreeze -> ready.units.map { it.id }
        DatePickerPurpose.FixExpiredDate ->
          ready.units.filter { it.status == ExpiryStatus.EXPIRED }.map { it.id }
      }
    viewModelScope.launch {
      if (purpose == DatePickerPurpose.Unfreeze) {
        unfreezeItem
          .unfreezeOn(ids.first(), date)
          .onSuccess {
            feedback(
              GlobalSnackbar(
                message = R.string.item_actions_snackbar_unfrozen,
                messageArgs = listOf(ready.name),
                type = AppFeedbackType.Success,
              ),
            )
          }.onFailure { failed() }
        return@launch
      }
      val snapshots = restoreItems.snapshots(ids)
      rescheduleItem
        .reschedule(ids, date)
        .onSuccess { feedback(rescheduledSnackbar(ready.name, snapshots)) }
        .onFailure { failed() }
    }
  }

  /** «Llença» with the − / + selector: the first [ItemActionsSheetState.Ready.selected] units. */
  override fun onDiscardSelected() {
    val ready = ready() ?: return
    discard(ready, ready.units.take(ready.selected).map { it.id })
  }

  /** «Llença» from «Més accions» of a product that has not expired: every unit. */
  override fun onDiscardAll() {
    val ready = ready() ?: return
    discard(ready, ready.units.map { it.id })
  }

  private fun discard(
    ready: ItemActionsSheetState.Ready,
    ids: List<String>,
  ) {
    eventTracker.trackAction(ItemActionAction("discard"))
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(ids)
      discardItems
        .discard(ids)
        .onSuccess {
          feedback(
            countedSnackbar(
              count = ids.size,
              one = R.string.status_snackbar_discarded,
              many = R.string.item_actions_snackbar_discarded_n,
              name = ready.name,
              type = AppFeedbackType.Info,
              snapshots = snapshots,
            ),
          )
        }.onFailure { failed() }
    }
  }

  override fun onMoveClick() {
    pickingCategory.value = true
  }

  override fun onMoveBack() {
    pickingCategory.value = false
  }

  override fun onMove(categoryId: String) {
    val ready = ready() ?: return
    val target = ready.otherCategories.firstOrNull { it.id == categoryId } ?: return
    eventTracker.trackAction(ItemActionAction("move"))
    val ids = ready.units.map { it.id }
    viewModelScope.launch {
      val failure = ids.firstNotNullOfOrNull { moveItem.move(it, categoryId).exceptionOrNull() }
      if (failure != null) {
        failed()
        return@launch
      }
      feedback(
        GlobalSnackbar(
          message = R.string.item_actions_snackbar_moved,
          messageArgs = listOf(ready.name, target.name),
          actionLabel = R.string.add_undo,
          onAction = { ids.forEach { moveItem.move(it, ready.category.id) } },
        ),
      )
    }
  }

  private fun rescheduledSnackbar(
    name: String,
    snapshots: List<ItemSnapshot>,
  ) = GlobalSnackbar(
    message = R.string.item_actions_snackbar_rescheduled,
    messageArgs = listOf(name),
    actionLabel = R.string.add_undo,
    onAction = { restoreItems.restoreSnapshots(snapshots) },
  )

  private fun countedSnackbar(
    count: Int,
    one: Int,
    many: Int,
    name: String,
    type: AppFeedbackType,
    snapshots: List<ItemSnapshot>,
  ) = GlobalSnackbar(
    message = if (count == 1) one else many,
    messageArgs = if (count == 1) listOf(name) else listOf(count, name),
    actionLabel = R.string.add_undo,
    type = type,
    onAction = { restoreItems.restoreSnapshots(snapshots) },
  )

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
