package com.alorma.caducity.ui.screen.dashboard

sealed class DashboardState {
  data object Loading : DashboardState()

  data class Success(
    val items: List<ProductUiModel>,
    val config: DashboardUI,
  ) : DashboardState()
}
