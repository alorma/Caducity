package com.alorma.caducity.dashboard

sealed class DashboardState {
  data object Loading : DashboardState()

  data class Success(
    val sections: List<DashboardSection>
  ) : DashboardState()
}
