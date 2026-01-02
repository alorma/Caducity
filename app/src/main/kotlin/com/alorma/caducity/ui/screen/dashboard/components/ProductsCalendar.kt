package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.caducity.ui.components.calendar.CaducityMonthCalendar
import com.alorma.caducity.ui.components.calendar.CaducityWeekCalendar
import com.alorma.caducity.ui.screen.dashboard.CalendarMode
import com.alorma.caducity.ui.screen.dashboard.CalendarState
import kotlinx.datetime.LocalDate

@Composable
fun ProductsCalendar(
  calendarState: CalendarState,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
  calendarMode: CalendarMode = CalendarMode.MONTH,
) {
  Box(modifier = modifier) {
    when (calendarMode) {
      CalendarMode.WEEK -> {
        CaducityWeekCalendar(
          calendarState = calendarState,
          onDateClick = onDateClick,
        )
      }

      CalendarMode.MONTH -> {
        CaducityMonthCalendar(
          calendarState = calendarState,
          onDateClick = onDateClick,
        )
      }
    }
  }
}
