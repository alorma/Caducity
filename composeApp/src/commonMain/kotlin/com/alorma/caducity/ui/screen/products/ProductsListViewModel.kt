package com.alorma.caducity.ui.screen.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

class ProductsListViewModel(
  obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val productsListMapper: ProductsListMapper,
) : ViewModel() {

  val filters: MutableStateFlow<ProductsListFilter> = MutableStateFlow(ProductsListFilter.All)

  val state: StateFlow<ProductsListState> = obtainDashboardProductsUseCase
    .obtainProducts()
    .combine(filters) { products, filter ->
      val filteredItems = productsListMapper.mapToProductsList(
        products = products,
        filter = filter,
      )

      ProductsListState.Success(
        items = filteredItems,
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = ProductsListState.Loading,
    )

  fun onFiltersUpdate(newFilters: ProductsListFilter) {
    filters.update { newFilters }
  }
}
