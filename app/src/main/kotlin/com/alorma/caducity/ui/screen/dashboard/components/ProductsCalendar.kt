package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.ui.components.calendar.CaducityMonthCalendar
import com.alorma.caducity.ui.components.calendar.CaducityWeekCalendar
import com.alorma.caducity.ui.screen.dashboard.CalendarData
import com.alorma.caducity.ui.screen.dashboard.CalendarMode
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun ProductsCalendar(
  calendarData: CalendarData,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
  calendarMode: CalendarMode = CalendarMode.MONTH,
  appClock: AppClock = koinInject(),
) {
  val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
  val currentMonth = YearMonth(today.year, today.month)

  // Calculate start and end months
  val currentMonthNum = today.month.ordinal + 1 // Month ordinal is 0-based
  val startMonthNum = currentMonthNum - 1
  val startMonth = if (startMonthNum >= 1) {
    YearMonth(today.year, startMonthNum)
  } else {
    YearMonth(today.year - 1, 12)
  }

  val endMonthNum = currentMonthNum + 3
  val endMonth = if (endMonthNum <= 12) {
    YearMonth(today.year, endMonthNum)
  } else {
    YearMonth(today.year + 1, endMonthNum - 12)
  }

  Box(modifier = modifier) {
    when (calendarMode) {
      CalendarMode.WEEK -> {
        CaducityWeekCalendar(
          startMonth = startMonth,
          endMonth = endMonth,
          today = today,
          calendarData = calendarData,
          onDateClick = onDateClick,
        )
      }

      CalendarMode.MONTH -> {
        CaducityMonthCalendar(
          startMonth = startMonth,
          endMonth = endMonth,
          today = today,
          calendarData = calendarData,
          onDateClick = onDateClick,
        )
      }
    }
  }
}