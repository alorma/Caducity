package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  showExpiringOnly: Boolean,
  obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardUiConfiguration: DashboardUiConfiguration,
  private val dashboardMapper: DashboardMapper,
) : ViewModel() {

  init {
    dashboardUiConfiguration.config.update { ui ->
      val filters = if (showExpiringOnly) {
        ui.statusFilters
          .toMutableSet()
          .apply {
            add(InstanceStatus.Expired)
            add(InstanceStatus.ExpiringSoon)
          }
          .toImmutableSet()
      } else {
        ui.statusFilters
      }

      ui.copy(statusFilters = filters)
    }
  }

  val state: StateFlow<DashboardState> = combine(
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

  fun updateSearchQuery(query: String) {
    dashboardUiConfiguration.updateSearchQuery(query)
  }

  fun updateStatusFilters(filters: Set<InstanceStatus>) {
    dashboardUiConfiguration.updateStatusFilters(filters)
  }
}
