package com.alorma.caducity.ui.screen.dashboard

sealed class DashboardState {
  data object Loading : DashboardState()

  data class Success(
    val data: DashboardModeState
  ) : DashboardState()
}

sealed interface DashboardModeState {
  data class Unified(
    val summary: DashboardSummary,
    val calendarState: CalendarState,
  ) : DashboardModeState

  data class PerProduct(
    val summary: DashboardSummary,
    val calendarState: CalendarState,
  ) : DashboardModeState
}