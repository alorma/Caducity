package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import com.alorma.caducity.ui.components.calendar.AppCalendarConfig

@Stable
data class DashboardSummary(
  val expired: Int,
  val expiringSoon: Int,
  val fresh: Int,
  val frozen: Int,
)

@Stable
data class CategoryCalendarState(
  val id: String,
  val name: String,
  val appCalendarConfig: AppCalendarConfig,
)
