package com.alorma.caducity.ui.screen.dashboard.filtered

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.GetItemsByStatusUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FilteredItemsByStatusViewModel(
  status: ItemStatus,
  getItemsByStatusUseCase: GetItemsByStatusUseCase,
) : ViewModel() {

  val state: StateFlow<FilteredItemsByStatusState> = getItemsByStatusUseCase.load(status)
    .map { categories ->
      if (categories.isEmpty()) {
        FilteredItemsByStatusState.Empty
      } else {
        FilteredItemsByStatusState.Success(categories)
      }
    }
    .catch { error ->
      emit(FilteredItemsByStatusState.Error(error.message ?: "Unknown error"))
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = FilteredItemsByStatusState.Loading
    )

  private val sideEffectChannel = Channel<FilteredItemsByStatusSideEffect>()
  val sideEffects = sideEffectChannel.receiveAsFlow()

  private fun emitSideEffect(effect: FilteredItemsByStatusSideEffect) {
    viewModelScope.launch {
      sideEffectChannel.send(effect)
    }
  }

  fun onProductClick(productName: String, items: List<Item>) {
    emitSideEffect(
      FilteredItemsByStatusSideEffect.ShowProductItemsBottomSheet(
        productName = productName,
        items = items,
      )
    )
  }

  fun onItemClick(item: Item) {
    emitSideEffect(FilteredItemsByStatusSideEffect.ShowItemActionsBottomSheet(item))
  }
}

sealed interface FilteredItemsByStatusState {
  data object Loading : FilteredItemsByStatusState
  data class Success(val categories: List<CategoryWithItems>) : FilteredItemsByStatusState
  data object Empty : FilteredItemsByStatusState
  data class Error(val message: String) : FilteredItemsByStatusState
}
