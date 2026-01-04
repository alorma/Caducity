package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

@Composable
fun CaducityWeekCalendar(
  calendarState: CalendarState,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {

  val weekCalendarState = rememberWeekCalendarState(
    startDate = calendarState.startMonth.firstDay,
    endDate = calendarState.endMonth.lastDay,
    firstDayOfWeek = calendarState.today.dayOfWeek,
  )

  WeekCalendar(
    modifier = modifier,
    state = weekCalendarState,
    contentPadding = PaddingValues(horizontal = 16.dp),
    weekHeader = { week ->
      val daysOfWeek = remember {
        week.days.map { weekDay ->
          weekDay.date.dayOfWeek
        }.toImmutableList()
      }

      CalendarHeader(
        calendarState = calendarState,
        daysOfWeek = daysOfWeek,
      )
    },
    dayContent = { weekDay ->
      val date = weekDay.date
      val dateInfo = calendarState.calendarData.productsByDate[date]

      DayContent(
        today = calendarState.today,
        date = date,
        status = dateInfo?.status,
        shapePosition = dateInfo?.shapePosition ?: ShapePosition.None,
        onClick = onDateClick,
        isOutDay = false,
      )
    },
  )
}