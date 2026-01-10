package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

@Composable
fun CaducityWeekCalendar(
  calendarState: CalendarState,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val weekCalendarState: WeekCalendarState = rememberWeekCalendarState(
    firstVisibleWeekDate = calendarState.today,
    startDate = calendarState.startDate,
    endDate = calendarState.endDate,
  )

  WeekCalendar(
    modifier = modifier.fillMaxWidth(),
    state = weekCalendarState,
    contentPadding = contentPadding,
    weekHeader = { week ->
      val weekDays = week.days.map { weekDay ->
        weekDay.date
      }.toImmutableList()
      CalendarWeekDaysHeader(
        weekDays = weekDays,
        dayOfWeekNames = calendarState.daysOfWeekNames,
      )
    },
    dayContent = { calendarDay ->
      val date = calendarDay.date
      val dateInfo = calendarState.content[date]

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