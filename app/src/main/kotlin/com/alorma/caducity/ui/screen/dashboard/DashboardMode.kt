package com.alorma.caducity.ui.screen.dashboard

sealed interface DashboardMode {
  data object Unified: DashboardMode
  data object PerProduct: DashboardMode
}