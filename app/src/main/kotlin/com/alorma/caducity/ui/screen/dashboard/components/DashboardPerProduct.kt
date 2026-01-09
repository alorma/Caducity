package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.DashboardState
import kotlinx.datetime.LocalDate

@Composable
fun DashboardPerProduct(
  state: DashboardState.Success.PerProduct,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  onNavigateToDate: (LocalDate) -> Unit
) {
  Text(text = "Potato")
}