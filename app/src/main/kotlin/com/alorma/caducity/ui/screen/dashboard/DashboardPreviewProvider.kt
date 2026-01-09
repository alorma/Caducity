package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.alorma.caducity.ui.components.calendar.calendarData
import com.alorma.caducity.ui.components.calendar.daysOfWeekNames
import com.alorma.caducity.ui.components.calendar.monthNames
import com.alorma.caducity.ui.components.calendar.today
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths

class DashboardPreviewProvider : CollectionPreviewParameterProvider<DashboardState>(
  listOf(
    DashboardState.Loading,
    DashboardState.Success.Unified(
      summary = DashboardSummary(
        expired = 6,
        expiringSoon = 1,
        fresh = 9,
        frozen = 8,
      ),
      calendarState = CalendarState(
        today = today,
        startLocalDate = today.minusMonths(2),
        endLocalDate = today.plusMonths(2),
        content = calendarData,
        monthNames = monthNames,
        daysOfWeekNames = daysOfWeekNames,
      ),
    ),
    DashboardState.Success.PerProduct(
      states = listOf(
        ProductCalendarState(
          id = "Potato1",
          name = "Potato 1",
          calendarState = CalendarState(
            today = today,
            startLocalDate = today.minusMonths(2),
            endLocalDate = today.plusMonths(2),
            content = calendarData,
            monthNames = monthNames,
            daysOfWeekNames = daysOfWeekNames,
          ),
        ),
        ProductCalendarState(
          id = "Potato2",
          name = "Potato 2",
          calendarState = CalendarState(
            today = today,
            startLocalDate = today.minusMonths(2),
            endLocalDate = today.plusMonths(2),
            content = calendarData,
            monthNames = monthNames,
            daysOfWeekNames = daysOfWeekNames,
          ),
        ),
      ),
    ),
  ),
)