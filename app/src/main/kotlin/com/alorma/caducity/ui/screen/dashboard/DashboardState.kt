package com.alorma.caducity.ui.screen.dashboard

sealed interface DashboardState {
  data object Loading : DashboardState

  sealed interface Success : DashboardState {
    val summary: DashboardSummary

    data class PerProduct(
      override val summary: DashboardSummary,
      val products: List<ProductCalendarState>,
    ) : Success
  }
}
