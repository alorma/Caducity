package com.alorma.caducity.ui.screen.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

class ProductsListViewModel(
  filtersParam: ProductsListFilter,
  obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val productsListMapper: ProductsListMapper,
) : ViewModel() {

  val filters: MutableStateFlow<ProductsListFilter> = MutableStateFlow(filtersParam)

  @OptIn(ExperimentalCoroutinesApi::class)
  val products: Flow<ImmutableList<ProductWithInstances>> = filters.flatMapConcat { filter ->
    obtainDashboardProductsUseCase.obtainProducts()
  }

  val state: StateFlow<ProductsListState> = combine(
    products,
    filters,
  ) { products, filter ->
    val filteredItems = productsListMapper.mapToProductsList(
      products = products,
    )

    if (filteredItems.isEmpty()) {
      ProductsListState.Empty(filter = filter)
    } else {
      ProductsListState.Success(items = filteredItems)
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5.seconds),
    initialValue = ProductsListState.Loading,
  )

  fun onFiltersUpdate(newFilters: ProductsListFilter) {
    filters.update { newFilters }
  }
}
