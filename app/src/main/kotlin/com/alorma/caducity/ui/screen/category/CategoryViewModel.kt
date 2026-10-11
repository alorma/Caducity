package com.alorma.caducity.ui.screen.category

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveWeekStripUseCase
import com.alorma.caducity.feature.tracking.CategoryDeletedAction
import com.alorma.caducity.feature.tracking.CategoryFilterAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToAddItemFromCategoryAction
import com.alorma.caducity.feature.tracking.NavigateToCalendarFromCategoryAction
import com.alorma.caducity.feature.tracking.NavigateToCategoryEditorAction
import com.alorma.caducity.feature.tracking.NavigateToItemActionsFromCategoryAction
import com.alorma.caducity.ui.base.BaseViewModel
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
  private val eventTracker: EventTracker,
) : BaseViewModel<CategoryNavigation, CategoryNavigationSideEffect, CategorySideEffect>() {
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

  fun onDeleteClick() {
    val current = state.value as? CategoryState.Ready ?: return
    emitSideEffect(CategorySideEffect.ShowDeleteConfirm(current.data.category.name, current.data.unitCount))
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
        emitNavigationSideEffect(CategoryNavigationSideEffect.OpenItemActions(navigation.itemId))
      }
    }
  }
}
