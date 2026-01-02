package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.dashboard.CalendarData
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

@Composable
fun CaducityWeekCalendar(
  startMonth: YearMonth,
  endMonth: YearMonth,
  today: LocalDate,
  calendarData: CalendarData,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {

  val weekCalendarState = rememberWeekCalendarState(
    startDate = startMonth.firstDay,
    endDate = endMonth.lastDay,
    firstDayOfWeek = today.dayOfWeek,
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

      val weekDates = remember {
        week.days.map { weekDay ->
          weekDay.date
        }.toImmutableList()
      }

      val firstMonth = week.days.first().date.month
      val lastMonth = week.days.last().date.month
      val firstYear = week.days.first().date.year
      val lastYear = week.days.last().date.year

      CalendarHeader(
        firstMonth = firstMonth,
        secondMonth = lastMonth,
        daysOfWeek = daysOfWeek,
        weekDates = weekDates,
        highlightToday = true,
        firstYear = firstYear,
        secondYear = lastYear,
      )
    },
    dayContent = { weekDay ->
      val date = weekDay.date
      val dateInfo = calendarData.productsByDate[date]

      DayContent(
        today = today,
        date = date,
        status = dateInfo?.status,
        shapePosition = dateInfo?.shapePosition ?: ShapePosition.None,
        onClick = onDateClick,
        isOutDay = false,
      )
    },
  )
}