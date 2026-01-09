package com.alorma.caducity.ui.screen.dashboard

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

data class DashboardConfigState(
  val mode: DashboardMode,
)

class DashboardConfigurator(
  private val settings: Settings,
) {

  val state: MutableStateFlow<DashboardConfigState> = MutableStateFlow(
    obtainCurrent()
  )

  private fun obtainCurrent(): DashboardConfigState {
    return DashboardConfigState(
      mode = when (settings.getString(KeyDashboardMode, DashboardModeUnified)) {
        DashboardModeUnified -> DashboardMode.Unified
        DashboardModePerProduct -> DashboardMode.PerProduct
        else -> DashboardMode.Unified
      }
    )
  }

  fun changeDashboardMode(mode: DashboardMode) {
    settings[KeyDashboardMode] = when (mode) {
      DashboardMode.Unified -> DashboardModeUnified
      DashboardMode.PerProduct -> DashboardModePerProduct
    }

    state.getAndUpdate { current ->
      current.copy(mode = mode)
    }
  }

  companion object {
    private const val KeyDashboardMode = "dashboard_mode"
    private const val DashboardModeUnified = "dashboard_mode_unified"
    private const val DashboardModePerProduct = "dashboard_mode_per_product"
  }
}

