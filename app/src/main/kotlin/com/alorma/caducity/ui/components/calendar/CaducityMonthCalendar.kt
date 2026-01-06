package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.Column
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
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minusMonth
import kotlinx.datetime.yearMonth

@Composable
fun CaducityMonthCalendar(
  calendarState: CalendarState,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val monthCalendarState = rememberCalendarState(
    startMonth = calendarState.today.yearMonth.minusMonth(),
    endMonth = calendarState.today.yearMonth.plusMonths(3),
    firstVisibleMonth = calendarState.today.yearMonth,
  )

  HorizontalCalendar(
    modifier = modifier.fillMaxWidth(),
    state = monthCalendarState,
    contentPadding = PaddingValues(horizontal = 16.dp),
    monthHeader = { calendarMonth ->
      Column {
        CalendarYearMonthHeader(
          startYearMonth = calendarMonth.yearMonth,
          endYearMonth = calendarMonth.weekDays.last().last().date.yearMonth,
          monthNames = calendarState.monthNames,
        )

        CalendarWeekDaysHeader(
          weekDays = calendarMonth.weekDays
            .first()
            .map { calendarDay -> calendarDay.date }
            .toImmutableList(),
          dayOfWeekNames = calendarState.daysOfWeekNames,
        )
      }
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
        isOutDay = calendarDay.position != DayPosition.MonthDate,
      )
    },
  )
}

@PreviewDynamicLightDark
@Composable
fun CaducityMonthCalendarPreview(
  @PreviewParameter(provider = CalendarStateProvider::class) calendarState: CalendarState,
) {
  PreviewTheme {
    Surface {
      CaducityMonthCalendar(
        calendarState = calendarState,
        onDateClick = {},
      )
    }
  }
}