package com.alorma.caducity.ui.components.calendar

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.dashboard.CalendarData
import com.alorma.caducity.ui.screen.dashboard.CalendarDateInfo
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import com.alorma.caducity.ui.screen.dashboard.DaysOfWeekNames
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.plusDays
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val daysOfWeekNames = DaysOfWeekNames(
  monday = "mon",
  tuesday = "tue",
  wednesday = "wed",
  thursday = "thu",
  friday = "fri",
  saturday = "sat",
  sunday = "sun",
)

private val month = YearMonth(2026, 2)
private val today = LocalDate(month.year, month.month, 6)

class CalendarStateProvider : CollectionPreviewParameterProvider<CalendarState>(
  listOf(
    CalendarState(
      today = today,
      calendarData = CalendarData(
        productsByDate = emptyMap<LocalDate, CalendarDateInfo>().toImmutableMap(),
      ),
      daysOfWeekNames = daysOfWeekNames,
    ),
    CalendarState(
      today = today,
      calendarData = CalendarData(
        productsByDate = mapOf(
          today to CalendarDateInfo(
            status = InstanceStatus.Expired, shapePosition = ShapePosition.Start,
          ),
          today.plusDays(1) to CalendarDateInfo(
            status = InstanceStatus.ExpiringSoon, shapePosition = ShapePosition.Middle,
          ),
          today.plusDays(2) to CalendarDateInfo(
            status = InstanceStatus.Frozen, shapePosition = ShapePosition.Middle,
          ),
          today.plusDays(3) to CalendarDateInfo(
            status = InstanceStatus.Fresh, shapePosition = ShapePosition.End,
          ),
          today.plusDays(5) to CalendarDateInfo(
            status = InstanceStatus.Fresh, shapePosition = ShapePosition.Single,
          ),
        ).toImmutableMap(),
      ),
      daysOfWeekNames = daysOfWeekNames,
    ),
  ),
)
