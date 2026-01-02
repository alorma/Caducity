package com.alorma.caducity.ui.screen.dashboard

import kotlinx.collections.immutable.ImmutableList

sealed class DashboardState {
  data object Loading : DashboardState()

  data class Success(
    val items: ImmutableList<ProductUiModel>,
    val summary: DashboardSummary,
    val config: DashboardUI,
    val calendarState: CalendarState,
  ) : DashboardState()
}

data class DashboardSummary(
  val expired: Int,
  val expiringSoon: Int,
  val fresh: Int,
  val frozen: Int,
)
