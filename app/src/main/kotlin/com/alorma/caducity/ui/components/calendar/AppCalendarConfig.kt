package com.alorma.caducity.ui.components.calendar

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.shape.ShapePosition
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames

@Stable
data class AppCalendarConfig(
  val today: LocalDate,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val content: ImmutableMap<LocalDate, AppCalendarDateInfo>,
  val monthNames: MonthNames,
  val daysOfWeekNames: DayOfWeekNames,
  val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
)

@Stable
data class AppCalendarDateInfo(
  val status: ItemStatus?,
  val shapePosition: ShapePosition,
)