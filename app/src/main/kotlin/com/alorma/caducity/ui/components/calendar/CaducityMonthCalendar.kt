package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.kizitonwose.calendar.compose.CalendarState
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
  appCalendarConfig: AppCalendarConfig,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
  monthCalendarState: CalendarState =
    rememberCalendarState(
      startMonth = appCalendarConfig.today.yearMonth.minusMonth(),
      endMonth = appCalendarConfig.today.yearMonth.plusMonths(3),
      firstVisibleMonth = appCalendarConfig.today.yearMonth,
      firstDayOfWeek = appCalendarConfig.firstDayOfWeek,
    ),
) {
  HorizontalCalendar(
    modifier = modifier.fillMaxWidth(),
    state = monthCalendarState,
    monthHeader = { calendarMonth ->
      Column {
        CalendarYearMonthHeader(
          startYearMonth = calendarMonth.yearMonth,
          endYearMonth =
            calendarMonth.weekDays
              .last()
              .last()
              .date.yearMonth,
          monthNames = appCalendarConfig.monthNames,
        )

        CalendarWeekDaysHeader(
          weekDays =
            calendarMonth.weekDays
              .first()
              .map { calendarDay -> calendarDay.date }
              .toImmutableList(),
          dayOfWeekNames = appCalendarConfig.daysOfWeekNames,
        )
      }
    },
    dayContent = { calendarDay ->
      val date = calendarDay.date
      val dateInfo = appCalendarConfig.content[date]

      DayContent(
        today = appCalendarConfig.today,
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
  @PreviewParameter(provider = CalendarStateProvider::class) appCalendarConfig: AppCalendarConfig,
) {
  PreviewTheme {
    Surface {
      CaducityMonthCalendar(
        appCalendarConfig = appCalendarConfig,
        onDateClick = {},
      )
    }
  }
}
