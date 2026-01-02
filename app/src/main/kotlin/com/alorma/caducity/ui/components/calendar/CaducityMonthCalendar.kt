package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth

@Composable
fun CaducityMonthCalendar(
  calendarState: CalendarState,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val monthCalendarState = rememberCalendarState(
    startMonth = calendarState.startMonth,
    endMonth = calendarState.endMonth,
    firstVisibleMonth = calendarState.today.yearMonth,
  )

  HorizontalCalendar(
    modifier = modifier.fillMaxWidth(),
    state = monthCalendarState,
    contentPadding = PaddingValues(horizontal = 16.dp),
    monthHeader = { calendarMonth ->
      val daysOfWeek = remember {
        calendarMonth.weekDays.first().map { weekDay ->
          weekDay.date.dayOfWeek
        }.toImmutableList()
      }

      val firstMonth = calendarMonth.yearMonth.month
      val lastMonth = calendarMonth.weekDays.last().last().date.month
      val firstYear = calendarMonth.weekDays.first().first().date.year
      val lastYear = calendarMonth.weekDays.last().last().date.year

      CalendarHeader(
        firstMonth = firstMonth,
        secondMonth = lastMonth,
        daysOfWeek = daysOfWeek,
        firstYear = firstYear,
        secondYear = lastYear,
      )
    },
    dayContent = { calendarDay ->
      val date = calendarDay.date
      val dateInfo = calendarState.calendarData.productsByDate[date]

      DayContent(
        today = calendarState.today,
        date = date,
        status = dateInfo?.status,
        shapePosition = dateInfo?.shapePosition ?: ShapePosition.None,
        onClick = onDateClick,
        isOutDay = calendarDay.position != DayPosition.MonthDate,
      )
    },
  )
}