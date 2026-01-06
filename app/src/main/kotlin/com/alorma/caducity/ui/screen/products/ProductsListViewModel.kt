package com.alorma.caducity.ui.screen.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ProductsListFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class ProductsListViewModel(
  filtersParam: ProductsListFilter,
  private val productsListMapper: ProductsListMapper,
) : ViewModel() {

  val state: StateFlow<ProductsListState> = MutableStateFlow(ProductsListState.Loading)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = ProductsListState.Loading,
    )

  fun onFiltersUpdate(newFilters: ProductsListFilter) {

  }
}
