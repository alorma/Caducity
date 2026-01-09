package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.calendar.CaducityMonthCalendar
import com.alorma.caducity.ui.screen.dashboard.DashboardState
import kotlinx.datetime.LocalDate

@Composable
fun DashboardUnified(
  state: DashboardState.Success.Unified,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  onNavigateToDate: (LocalDate) -> Unit
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(bottom = 64.dp),
  ) {

    item(
      key = "summary",
      contentType = "summary"
    ) {
      DashboardSummaryCard(
        summary = state.summary,
        onStatusClick = { status -> onNavigateToStatus(status) },
      )
    }

    item(contentType = "calendar") {
      CaducityMonthCalendar(
        calendarState = state.calendarState,
        onDateClick = onNavigateToDate,
      )
    }
  }
}