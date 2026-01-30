package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.calendar.AppCalendarConfig
import com.alorma.caducity.ui.components.shape.ShapePosition
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.LocalDate

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
