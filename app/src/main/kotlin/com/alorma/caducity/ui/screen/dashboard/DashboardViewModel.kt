package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  private val dashboardConfigurator: DashboardConfigurator,
  obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardMapper: DashboardMapper,
) : ViewModel() {

  val state: StateFlow<DashboardState> =
    combine(
      dashboardConfigurator.state,
      obtainDashboardProductsUseCase
        .obtainProducts()
    ) { dashboardState, instances ->
      dashboardMapper.mapToDashboardState(
        dashboardState = dashboardState,
        instances = instances,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )


  fun changeDashboardMode(mode: DashboardMode) = viewModelScope.launch {
    dashboardConfigurator.changeDashboardMode(mode)
  }
}
