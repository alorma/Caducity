package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.domain.usecase.ProductsListFilter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardUiConfiguration: DashboardUiConfiguration,
  private val dashboardMapper: DashboardMapper,
) : ViewModel() {

  val state: StateFlow<DashboardState> = combine(
    obtainDashboardProductsUseCase.obtainProducts(ProductsListFilter.All),
    dashboardUiConfiguration.config,
  ) { products, config ->
    val dashboardData = dashboardMapper.mapToDashboardSections(
      products = products,
      searchQuery = config.searchQuery,
      statusFilters = config.statusFilters,
    )
    DashboardState.Success(
      items = dashboardData.items,
      summary = dashboardData.summary,
      config = config,
      calendarState = dashboardData.calendarState,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5.seconds),
    initialValue = DashboardState.Loading,
  )

  fun updateSearchQuery(query: String) {
    dashboardUiConfiguration.updateSearchQuery(query)
  }

  fun updateStatusFilters(filters: Set<InstanceStatus>) {
    dashboardUiConfiguration.updateStatusFilters(filters)
  }

  fun toggleCalendarMode() {
    dashboardUiConfiguration.toggleCalendarMode()
  }
}
