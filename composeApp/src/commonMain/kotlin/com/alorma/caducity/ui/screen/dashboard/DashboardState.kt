package com.alorma.caducity.ui.screen.dashboard

sealed class DashboardState {
  data object Loading : DashboardState()

  data class Success(
    val sections: List<DashboardSection>
  ) : DashboardState()

  data class Error(
    val message: String
  ) : DashboardState()
}
