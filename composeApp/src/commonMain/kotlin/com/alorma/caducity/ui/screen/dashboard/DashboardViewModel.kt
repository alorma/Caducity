package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.delayEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardMapper: DashboardMapper
) : ViewModel() {

  val state: StateFlow<DashboardState> = obtainDashboardProductsUseCase
    .obtainProducts()
    .onEach { delay(5.milliseconds) }
    .map { dashboardProducts ->
      val sections = dashboardMapper.mapToDashboardSections(dashboardProducts)
      DashboardState.Success(sections = sections)
    }
    .catch { exception ->
      println("Alorma: Error loading dashboard: ${exception.message}")
      exception.printStackTrace()
      emit(DashboardState.Error(message = exception.message ?: "Unknown error"))
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )
}
