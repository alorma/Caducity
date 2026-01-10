package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.ui.components.calendar.AppCalendarConfig

sealed interface DashboardState {
  data object Loading : DashboardState

  sealed interface Success: DashboardState {
    val summary: DashboardSummary

    data class Unified(
      override val summary: DashboardSummary,
      val appCalendarConfig: AppCalendarConfig,
    ) : Success

    data class PerProduct(
      override val summary: DashboardSummary,
      val products: List<ProductCalendarState>,
    ) : Success
  }
}
