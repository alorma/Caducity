package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.alorma.caducity.ui.components.calendar.AppCalendarConfig
import com.alorma.caducity.ui.components.calendar.calendarData
import com.alorma.caducity.ui.components.calendar.daysOfWeekNames
import com.alorma.caducity.ui.components.calendar.monthNames
import com.alorma.caducity.ui.components.calendar.today
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths

internal val summary = DashboardSummary(
  expired = 6,
  expiringSoon = 1,
  fresh = 9,
  frozen = 8,
)

class DashboardPreviewProvider : CollectionPreviewParameterProvider<DashboardState>(
  listOf(
    DashboardState.Loading,
    DashboardState.Success.PerCategory(
      summary = summary,
      categories = listOf(
        CategoryCalendarState(
          id = "Potato1",
          name = "Potato 1",
          appCalendarConfig = AppCalendarConfig(
            today = today,
            startDate = today.minusMonths(2),
            endDate = today.plusMonths(2),
            content = calendarData,
            monthNames = monthNames,
            daysOfWeekNames = daysOfWeekNames,
          ),
        ),
        CategoryCalendarState(
          id = "Potato2",
          name = "Potato 2",
          appCalendarConfig = AppCalendarConfig(
            today = today,
            startDate = today.minusMonths(2),
            endDate = today.plusMonths(2),
            content = calendarData,
            monthNames = monthNames,
            daysOfWeekNames = daysOfWeekNames,
          ),
        ),
      ),
    ),
  ),
)