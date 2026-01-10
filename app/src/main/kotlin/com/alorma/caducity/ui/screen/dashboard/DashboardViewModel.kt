package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  calendarPreferences: CalendarPreferences,
  private val obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardMapper: DashboardMapper,
) : ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  val state: StateFlow<DashboardState> = calendarPreferences.state
    .flatMapLatest { calendarConfig ->
      obtainPerProductDashboard(calendarConfig.firstDayOfWeek)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )

  private fun obtainPerProductDashboard(firstDayOfWeek: kotlinx.datetime.DayOfWeek): Flow<DashboardState.Success> {
    return obtainDashboardProductsUseCase
      .obtainProducts()
      .map { products ->
        dashboardMapper.mapToPerProductState(products = products, firstDayOfWeek = firstDayOfWeek)
      }
  }
}
