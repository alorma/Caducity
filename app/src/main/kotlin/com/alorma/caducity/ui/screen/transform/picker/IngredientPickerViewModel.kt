package com.alorma.caducity.ui.screen.transform.picker

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.ExpiryStatus
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
  private val results: IngredientPickerResults,
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
        results.deliver(
          current.groups.chosenItemIds(current.selectedKeys, args.selectedItemIds, current.statusFilters),
        )
        emitNavigationSideEffect(IngredientPickerNavigationSideEffect.Close)
      }
    }
  }

  fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

  fun onStatusFilterToggle(status: ExpiryStatus) =
    _state.update { it.copy(statusFilters = it.statusFilters.toggled(status)) }

  fun onCategoryFilterToggle(categoryId: String) =
    _state.update { it.copy(categoryFilters = it.categoryFilters.toggled(categoryId)) }

  fun onToggle(key: String) =
    _state.update {
      val selected = if (key in it.selectedKeys) it.selectedKeys - key else it.selectedKeys + key
      it.copy(selectedKeys = selected)
    }
}

private fun <T> Set<T>.toggled(value: T): Set<T> = if (value in this) this - value else this + value
