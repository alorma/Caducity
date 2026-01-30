package com.alorma.caducity.ui.screen.dashboard

sealed interface DashboardState {
  data object Loading : DashboardState

  sealed interface Success : DashboardState {
    val summary: DashboardSummary

    data class PerCategory(
      override val summary: DashboardSummary,
      val categories: List<CategoryCalendarState>,
    ) : Success
  }
}
