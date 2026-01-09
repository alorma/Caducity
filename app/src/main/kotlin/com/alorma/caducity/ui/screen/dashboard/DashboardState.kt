package com.alorma.caducity.ui.screen.dashboard

import kotlinx.datetime.LocalDate

sealed interface DashboardState {
  data object Loading : DashboardState

  sealed interface Success: DashboardState {
    data class Unified(
      val summary: DashboardSummary,
      val calendarState: CalendarState,
    ) : Success

    data class PerProduct(
      val products: List<ProductCalendarState>,
    ) : Success
  }
}
