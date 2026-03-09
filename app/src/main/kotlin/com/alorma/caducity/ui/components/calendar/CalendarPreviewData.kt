package com.alorma.caducity.ui.components.calendar

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusDays
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames

val monthNames =
  MonthNames(
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

val daysOfWeekNames =
  DayOfWeekNames(
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

val calendarData =
  persistentMapOf(
    today to
      AppCalendarDateInfo(
        status = ItemStatus.Expired,
        shapePosition = ShapePosition.Start,
      ),
    today.plusDays(1) to
      AppCalendarDateInfo(
        status = ItemStatus.ExpiringSoon,
        shapePosition = ShapePosition.Middle,
      ),
    today.plusDays(2) to
      AppCalendarDateInfo(
        status = ItemStatus.Frozen,
        shapePosition = ShapePosition.Middle,
      ),
    today.plusDays(3) to
      AppCalendarDateInfo(
        status = ItemStatus.Frozen,
        shapePosition = ShapePosition.End,
      ),
    today.plusDays(5) to
      AppCalendarDateInfo(
        status = ItemStatus.Fresh,
        shapePosition = ShapePosition.Single,
      ),
  )

class CalendarStateProvider :
  CollectionPreviewParameterProvider<AppCalendarConfig>(
    listOf(
      AppCalendarConfig(
        today = today,
        startDate = today.minusMonths(2),
        endDate = today.plusMonths(2),
        content = emptyMap<LocalDate, AppCalendarDateInfo>().toImmutableMap(),
        daysOfWeekNames = daysOfWeekNames,
        monthNames = monthNames,
      ),
      AppCalendarConfig(
        today = today,
        startDate = today.minusMonths(2),
        endDate = today.plusMonths(2),
        content = calendarData,
        daysOfWeekNames = daysOfWeekNames,
        monthNames = monthNames,
      ),
    ),
  )
