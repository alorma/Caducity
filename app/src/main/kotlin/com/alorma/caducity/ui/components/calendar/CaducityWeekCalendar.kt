package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth

@Composable
fun CaducityWeekCalendar(
  calendarState: CalendarState,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {

  val weekCalendarState = rememberWeekCalendarState(
    startDate = calendarState.today.minusMonths(1),
    endDate = calendarState.today.plusMonths(3),
    firstVisibleWeekDate = calendarState.today,
    firstDayOfWeek = calendarState.today.dayOfWeek,
  )

  WeekCalendar(
    modifier = modifier,
    state = weekCalendarState,
    contentPadding = PaddingValues(horizontal = 16.dp),
    weekHeader = { week ->
      CalendarHeader(
        startYearMonth = week.days.first().date.yearMonth,
        endYearMonth = week.days.last().date.yearMonth,
        monthNames = calendarState.monthNames,
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

@PreviewDynamicLightDark
@Composable
fun CaducityWeekCalendarPreview(
  @PreviewParameter(provider = CalendarStateProvider::class) calendarState: CalendarState,
) {
  PreviewTheme {
    Surface {
      CaducityWeekCalendar(
        calendarState = calendarState,
        onDateClick = {},
      )
    }
  }
}