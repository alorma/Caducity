package com.alorma.caducity.ui.screen.category

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveWeekStripUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.feature.tracking.CategoryDeletedAction
import com.alorma.caducity.feature.tracking.CategoryFilterAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.ItemActionAction
import com.alorma.caducity.feature.tracking.NavigateToAddItemFromCategoryAction
import com.alorma.caducity.feature.tracking.NavigateToCalendarFromCategoryAction
import com.alorma.caducity.feature.tracking.NavigateToCategoryEditorAction
import com.alorma.caducity.feature.tracking.NavigateToItemActionsFromCategoryAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.actions.lowerWithSnackbar
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.SnackbarRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class CategoryViewModel(
  private val args: CategoryArgs,
  observeCategory: ObserveCategoryUseCase,
  observeWeekStrip: ObserveWeekStripUseCase,
  private val deleteCategory: DeleteCategoryUseCase,
  private val consumeItem: ConsumeItemUseCase,
  private val freezeItem: FreezeItemUseCase,
  private val unfreezeItem: UnfreezeItemUseCase,
  private val discardItems: DiscardItemsUseCase,
  private val restoreItems: RestoreItemsUseCase,
  private val eventTracker: EventTracker,
) : BaseViewModel<CategoryNavigation, CategoryNavigationSideEffect, CategorySideEffect>() {
  private fun showSnackbar(request: SnackbarRequest) = emitSideEffect(CategorySideEffect.ShowSnackbar(request))

  private val filter = MutableStateFlow(args.filter)
  private val selectedDay = MutableStateFlow<LocalDate?>(null)

  val state: StateFlow<CategoryState> =
    combine(
      observeCategory.observe(args.categoryId),
      observeWeekStrip.observe(args.categoryId),
      filter,
      selectedDay,
    ) { data, weekStrip, status, day ->
      if (data == null) {
        CategoryState.NotFound
      } else {
        CategoryState.Ready(
          data = data,
          weekStrip = weekStrip,
          filter = status,
          selectedDay = day,
          highlightProductId = args.highlightProductId,
        )
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryState.Loading)

  fun onFilterSelected(status: ExpiryStatus?) {
    eventTracker.trackAction(CategoryFilterAction(status?.name?.lowercase() ?: "all"))
    filter.value = status
  }

  /** Tapping the selected day again clears it. */
  fun onDayClick(date: LocalDate) {
    selectedDay.value = if (selectedDay.value == date) null else date
  }

  // region Round buttons of a row: one unit, the one that expires first

  /** ✓ marks **one** unit of the row as consumed, with undo. */
  fun onConsume(row: ProductRowModel) {
    if (row.unitType.parent == UnitParent.HANDFULS) {
      eventTracker.trackAction(ItemActionAction("lower_level_category"))
      viewModelScope.launch {
        consumeItem
          .lowerWithSnackbar(row.nearestItemId, row.name, row.level, restoreItems)
          .onSuccess { showSnackbar(it) }
          .onFailure { showSnackbar(SnackbarRequest(R.string.item_actions_error, type = AppFeedbackType.Error)) }
      }
      return
    }
    eventTracker.trackAction(ItemActionAction("consume_category"))
    undoable(
      ids = listOf(row.nearestItemId),
      snackbar = { undo ->
        SnackbarRequest(
          message = R.string.item_actions_snackbar_consumed,
          messageArgs = listOf(row.name),
          actionLabel = R.string.add_undo,
          type = AppFeedbackType.Success,
          onAction = undo,
        )
      },
    ) { consumeItem.consume(listOf(row.nearestItemId)) }
  }

  /** ❄ freezes **one** unit of the row, with undo. */
  fun onFreeze(row: ProductRowModel) {
    eventTracker.trackAction(ItemActionAction("freeze_category"))
    undoable(
      ids = listOf(row.nearestItemId),
      snackbar = { undo ->
        SnackbarRequest(
          message = R.string.item_actions_snackbar_frozen,
          messageArgs = listOf(row.name),
          actionLabel = R.string.add_undo,
          type = AppFeedbackType.Success,
          onAction = undo,
        )
      },
    ) { freezeItem.freeze(listOf(row.nearestItemId)) }
  }

  /** ☀ unfreezes **one** unit of the row (the one frozen the longest), with undo. */
  fun onUnfreeze(row: ProductRowModel) {
    eventTracker.trackAction(ItemActionAction("unfreeze_category"))
    undoable(
      ids = listOf(row.nearestItemId),
      snackbar = { undo ->
        SnackbarRequest(
          message = R.string.snack_unfrozen,
          messageArgs = listOf(1, row.name),
          actionLabel = R.string.add_undo,
          type = AppFeedbackType.Success,
          onAction = undo,
        )
      },
    ) { unfreezeItem.unfreeze(listOf(row.nearestItemId)) }
  }

  /** Paperera discards **one** unit of the row, with undo. */
  fun onDiscard(row: ProductRowModel) {
    eventTracker.trackAction(ItemActionAction("discard_category"))
    undoable(
      ids = listOf(row.nearestItemId),
      snackbar = { undo ->
        SnackbarRequest(
          message = R.string.status_snackbar_discarded,
          messageArgs = listOf(row.name),
          actionLabel = R.string.add_undo,
          type = AppFeedbackType.Info,
          onAction = undo,
        )
      },
    ) { discardItems.discard(listOf(row.nearestItemId)) }
  }

  /** Runs [action] and, if it worked, shows the snackbar whose «Desfés» puts the units back as they were. */
  private fun undoable(
    ids: List<String>,
    snackbar: (undo: suspend () -> Unit) -> SnackbarRequest,
    action: suspend () -> Result<*>,
  ) {
    viewModelScope.launch {
      val snapshots = restoreItems.snapshots(ids)
      action()
        .onSuccess { showSnackbar(snackbar { restoreItems.restoreSnapshots(snapshots) }) }
        .onFailure { showSnackbar(SnackbarRequest(R.string.item_actions_error, type = AppFeedbackType.Error)) }
    }
  }

  // endregion

  fun onDeleteClick() {
    val current = state.value as? CategoryState.Ready ?: return
    emitSideEffect(CategorySideEffect.ShowDeleteConfirm(current.data.category.name, current.data.productCount))
  }

  fun onDeleteConfirmed() {
    viewModelScope.launch {
      deleteCategory
        .deleteCategory(args.categoryId)
        .onSuccess {
          eventTracker.trackAction(CategoryDeletedAction())
          emitNavigationSideEffect(CategoryNavigationSideEffect.Close)
        }.onFailure { emitSideEffect(CategorySideEffect.DeleteFailed) }
    }
  }

  override fun navigate(navigation: CategoryNavigation) {
    when (navigation) {
      is CategoryNavigation.AddItem -> {
        eventTracker.trackAction(NavigateToAddItemFromCategoryAction(navigation.source))
        emitNavigationSideEffect(CategoryNavigationSideEffect.OpenAdd)
      }
      CategoryNavigation.Calendar -> {
        eventTracker.trackAction(NavigateToCalendarFromCategoryAction())
        emitNavigationSideEffect(CategoryNavigationSideEffect.OpenCalendar)
      }
      CategoryNavigation.Edit -> {
        eventTracker.trackAction(NavigateToCategoryEditorAction())
        emitNavigationSideEffect(CategoryNavigationSideEffect.OpenEditor)
      }
      is CategoryNavigation.ItemActions -> {
        eventTracker.trackAction(NavigateToItemActionsFromCategoryAction())
        emitNavigationSideEffect(CategoryNavigationSideEffect.OpenItemActions(navigation.itemIds))
      }
    }
  }
}
