package com.alorma.caducity.ui.screen.dashboard

import kotlinx.datetime.LocalDate

sealed interface DashboardState {
  data object Loading : DashboardState

  sealed interface Success: DashboardState {
    val summary: DashboardSummary

    data class Unified(
      override val summary: DashboardSummary,
      val calendarState: CalendarState,
    ) : Success

    data class PerProduct(
      override val summary: DashboardSummary,
      val products: List<ProductCalendarState>,
    ) : Success
  }
}
