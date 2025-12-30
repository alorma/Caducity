package com.alorma.caducity.ui.screen.dashboard.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.ui.screen.dashboard.DashboardMapper
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
  private val dashboardMapper: DashboardMapper,
) : ViewModel() {

  val filters: MutableStateFlow<ProductsListFilter> = MutableStateFlow(ProductsListFilter.All)

  val state: StateFlow<DateProductsState> = obtainDashboardProductsUseCase
    .obtainProducts()
    .combine(filters) { products, filters ->
      val dashboardData = dashboardMapper.mapToDashboardSections(
        products = products,
        searchQuery = "",
        statusFilters = emptySet(),
      )

      val filteredItems = filterProducts(
        products = dashboardData.items,
        filter = filters,
      )

      DateProductsState.Success(
        items = filteredItems,
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DateProductsState.Loading,
    )

  fun onFiltersUpdate(newFilters: ProductsListFilter) {
    filters.update { newFilters }
  }

  private fun filterProducts(
    products: ImmutableList<ProductUiModel>,
    filter: ProductsListFilter,
  ): ImmutableList<ProductUiModel> {
    return when (filter) {
      is ProductsListFilter.ByDate -> {
        products.filter { product ->
          if (product is ProductUiModel.WithInstances) {
            product.instances.any { instance ->
              instance.expirationDate == filter.date
            }
          } else {
            false
          }
        }.toImmutableList()
      }

      is ProductsListFilter.ByStatus -> {
        products.filter { product ->
          if (product is ProductUiModel.WithInstances) {
            product.instances.any { instance ->
              filter.statuses.contains(instance.status)
            }
          } else {
            false
          }
        }.toImmutableList()
      }

      is ProductsListFilter.ByDateRange -> {
        products.filter { product ->
          if (product is ProductUiModel.WithInstances) {
            product.instances.any { instance ->
              instance.expirationDate >= filter.startDate &&
                  instance.expirationDate <= filter.endDate
            }
          } else {
            false
          }
        }.toImmutableList()
      }

      ProductsListFilter.All -> products
    }
  }
}

sealed class DateProductsState {
  data object Loading : DateProductsState()

  data class Success(
    val items: ImmutableList<ProductUiModel>,
  ) : DateProductsState()
}
