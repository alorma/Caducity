package com.alorma.caducity.ui.components.calendar

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.dashboard.CalendarData
import com.alorma.caducity.ui.screen.dashboard.CalendarDateInfo
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusDays
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames

val monthNames = MonthNames(
  january = "January",
  february = "February",
  march = "March",
  april = "April",
  may = "May",
  june = "June",
  july = "July",
  august = "August",
  september = "September",
  october = "October",
  november = "November",
  december = "December",
)

val daysOfWeekNames = DayOfWeekNames(
  monday = "mon",
  tuesday = "tue",
  wednesday = "wed",
  thursday = "thu",
  friday = "fri",
  saturday = "sat",
  sunday = "sun",
)

val month = YearMonth(2026, Month.FEBRUARY)
val today = LocalDate(month.year, month.month, 9)

val calendarData = CalendarData(
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
      status = InstanceStatus.Frozen, shapePosition = ShapePosition.End,
    ),
    today.plusDays(5) to CalendarDateInfo(
      status = InstanceStatus.Fresh, shapePosition = ShapePosition.Single,
    ),
  ).toImmutableMap(),
)

class CalendarStateProvider : CollectionPreviewParameterProvider<CalendarState>(
  listOf(
    CalendarState(
      today = today,
      startLocalDate = today.minusMonths(2),
      endLocalDate = today.plusMonths(2),
      content = CalendarData(
        productsByDate = emptyMap<LocalDate, CalendarDateInfo>().toImmutableMap(),
      ),
      daysOfWeekNames = daysOfWeekNames,
      monthNames = monthNames,
    ),
    CalendarState(
      today = today,
      startLocalDate = today.minusMonths(2),
      endLocalDate = today.plusMonths(2),
      content = calendarData,
      daysOfWeekNames = daysOfWeekNames,
      monthNames = monthNames,
    ),
  ),
)
