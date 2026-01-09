package com.alorma.caducity.ui.screen.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  private val dashboardConfigurator: DashboardConfigurator,
  private val obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardMapper: DashboardMapper,
) : ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  val state: StateFlow<DashboardState> = dashboardConfigurator
    .state
    .transformLatest { dashboardConfig ->
      Log.i("Alorma", dashboardConfig.toString())

      emit(DashboardState.Loading)

      val data = when (dashboardConfig.mode) {
        DashboardMode.Unified -> obtainUnifiedDashboard()
        DashboardMode.PerProduct -> obtainPerProductDashboard()
      }
      emitAll(data)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )

  private fun obtainUnifiedDashboard(): Flow<DashboardState.Success> {
    return obtainDashboardProductsUseCase
      .obtainProducts()
      .map { instances ->
        dashboardMapper.mapToUnifiedState(instances = instances)
      }
  }

  private fun obtainPerProductDashboard(): Flow<DashboardState.Success> {
    return obtainDashboardProductsUseCase
      .obtainProducts()
      .map { instances ->
        dashboardMapper.mapToPerProductState(instances = instances)
      }
  }

  fun changeDashboardMode(mode: DashboardMode) = viewModelScope.launch {
    dashboardConfigurator.changeDashboardMode(mode)
  }
}
