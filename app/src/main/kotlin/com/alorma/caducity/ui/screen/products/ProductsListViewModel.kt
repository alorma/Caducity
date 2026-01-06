package com.alorma.caducity.ui.screen.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainProductsUseCase
import com.alorma.caducity.domain.usecase.ProductsListFilter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class ProductsListViewModel(
  filtersParam: ProductsListFilter,
  obtainProductsUseCase: ObtainProductsUseCase,
  private val productsListMapper: ProductsListMapper,
) : ViewModel() {

  val state: StateFlow<ProductsListState> =
    obtainProductsUseCase.obtain()
      .map { items ->
        if (items.isEmpty()) {
          ProductsListState.Empty(filtersParam)
        } else {
          ProductsListState.Success(
            items = productsListMapper.mapToProductsList(items),
          )
        }
      }
      .onStart { emit(ProductsListState.Loading) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = ProductsListState.Loading,
      )
}
