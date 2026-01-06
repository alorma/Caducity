package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.shape.ShapePosition
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames

@Stable
data class DashboardSummary(
  val expired: Int,
  val expiringSoon: Int,
  val fresh: Int,
  val frozen: Int,
)

@Stable
data class CalendarState(
  val today: LocalDate,
  val startLocalDate: LocalDate,
  val endLocalDate: LocalDate,
  val content: CalendarData,
  val monthNames: MonthNames,
  val daysOfWeekNames: DayOfWeekNames,
)

@Stable
data class CalendarData(
  val productsByDate: ImmutableMap<LocalDate, CalendarDateInfo>
)

@Stable
data class CalendarDateInfo(
  val status: InstanceStatus?,
  val shapePosition: ShapePosition,
)
