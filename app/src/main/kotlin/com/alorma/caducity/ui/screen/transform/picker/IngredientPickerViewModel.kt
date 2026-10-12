package com.alorma.caducity.ui.screen.transform.picker

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IngredientPickerViewModel(
  private val args: IngredientPickerArgs,
  observeItems: ObserveItemsUseCase,
  observeCategories: ObserveCategoriesUseCase,
) : BaseViewModel<IngredientPickerNavigation, IngredientPickerNavigationSideEffect, Unit>() {
  private val _state = MutableStateFlow(IngredientPickerState())
  val state: StateFlow<IngredientPickerState> = _state.asStateFlow()

  init {
    viewModelScope.launch {
      val groups = observeItems.observe().first().toPickerGroups()
      _state.update { it.copy(groups = groups, selectedKeys = groups.keysOf(args.selectedItemIds)) }
    }
    viewModelScope.launch {
      observeCategories.observe().collect { categories -> _state.update { it.copy(categories = categories) } }
    }
  }

  override fun navigate(navigation: IngredientPickerNavigation) {
    when (navigation) {
      IngredientPickerNavigation.Close -> emitNavigationSideEffect(IngredientPickerNavigationSideEffect.Close)
      IngredientPickerNavigation.Confirm -> {
        val current = _state.value
        emitNavigationSideEffect(
          IngredientPickerNavigationSideEffect.Confirm(current.groups.nearestItemIds(current.selectedKeys)),
        )
      }
    }
  }

  fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

  fun onStatusFilterSelect(filter: PickerStatusFilter) = _state.update { it.copy(statusFilter = filter) }

  /** Tapping the active category chip clears it. */
  fun onCategoryFilterSelect(categoryId: String) =
    _state.update { it.copy(categoryFilter = if (it.categoryFilter == categoryId) null else categoryId) }

  fun onToggle(key: String) =
    _state.update {
      val selected = if (key in it.selectedKeys) it.selectedKeys - key else it.selectedKeys + key
      it.copy(selectedKeys = selected)
    }
}
