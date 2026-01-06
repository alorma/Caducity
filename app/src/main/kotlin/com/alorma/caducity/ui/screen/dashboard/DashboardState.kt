package com.alorma.caducity.ui.screen.dashboard

sealed class DashboardState {
  data object Loading : DashboardState()

  data class Success(
    val summary: DashboardSummary,
    val calendarState: CalendarState,
  ) : DashboardState()
}