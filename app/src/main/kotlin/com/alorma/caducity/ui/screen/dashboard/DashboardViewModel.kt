package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardInstancesUseCase
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  private val dashboardConfigurator: DashboardConfigurator,
  private val calendarPreferences: CalendarPreferences,
  private val obtainDashboardInstancesUseCase: ObtainDashboardInstancesUseCase,
  private val obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardMapper: DashboardMapper,
) : ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  val state: StateFlow<DashboardState> = combine(
    dashboardConfigurator.state,
    calendarPreferences.state,
  ) { dashboardConfig, calendarConfig ->
    Pair(dashboardConfig, calendarConfig)
  }
    .flatMapLatest { (dashboardConfig, calendarConfig) ->
      when (dashboardConfig.mode) {
        DashboardMode.Unified -> obtainUnifiedDashboard(calendarConfig.firstDayOfWeek)
        DashboardMode.PerProduct -> obtainPerProductDashboard(calendarConfig.firstDayOfWeek)
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )

  private fun obtainUnifiedDashboard(firstDayOfWeek: kotlinx.datetime.DayOfWeek): Flow<DashboardState.Success> {
    return obtainDashboardInstancesUseCase
      .obtainInstances()
      .map { instances ->
        dashboardMapper.mapToUnifiedState(instances = instances, firstDayOfWeek = firstDayOfWeek)
      }
  }

  private fun obtainPerProductDashboard(firstDayOfWeek: kotlinx.datetime.DayOfWeek): Flow<DashboardState.Success> {
    return obtainDashboardProductsUseCase
      .obtainProducts()
      .map { products ->
        dashboardMapper.mapToPerProductState(products = products, firstDayOfWeek = firstDayOfWeek)
      }
  }

  fun changeDashboardMode(mode: DashboardMode) = viewModelScope.launch {
    dashboardConfigurator.changeDashboardMode(mode)
  }
}
