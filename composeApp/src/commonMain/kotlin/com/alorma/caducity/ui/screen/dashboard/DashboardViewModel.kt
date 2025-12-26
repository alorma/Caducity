package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
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

  val state: StateFlow<DashboardState> =
    combine(
      obtainDashboardProductsUseCase.obtainProducts(),
      dashboardUiConfiguration.config,
    ) { products, config ->
      val sections = dashboardMapper.mapToDashboardSections(
        products = products,
        searchQuery = config.searchQuery,
        statusFilters = config.statusFilters,
      )
      DashboardState.Success(
        items = sections,
        config = config,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )

  fun toggleExpanded(collapsed: Boolean) {
    dashboardUiConfiguration.updateCollapse(collapsed)
  }

  fun updateSearchQuery(query: String) {
    dashboardUiConfiguration.updateSearchQuery(query)
  }

  fun updateStatusFilters(filters: Set<InstanceStatus>) {
    dashboardUiConfiguration.updateStatusFilters(filters)
  }
}
