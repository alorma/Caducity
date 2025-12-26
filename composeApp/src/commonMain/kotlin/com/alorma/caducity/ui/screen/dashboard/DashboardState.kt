package com.alorma.caducity.ui.screen.dashboard

import kotlinx.collections.immutable.ImmutableList

sealed class DashboardState {
  data object Loading : DashboardState()

  data class Success(
    val items: ImmutableList<ProductUiModel>,
    val config: DashboardUI,
  ) : DashboardState()
}
