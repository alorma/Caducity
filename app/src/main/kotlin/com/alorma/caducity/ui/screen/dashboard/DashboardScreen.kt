package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.ui.components.calendar.CaducityMonthCalendar
import com.alorma.caducity.ui.components.calendar.calendarData
import com.alorma.caducity.ui.components.calendar.daysOfWeekNames
import com.alorma.caducity.ui.components.calendar.monthNames
import com.alorma.caducity.ui.components.calendar.today
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.screen.dashboard.components.DashboardSummaryCard
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
  viewModel: DashboardViewModel = koinViewModel(),
) {
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()

  Box(modifier) {
    when (val state = dashboardState.value) {
      is DashboardState.Loading -> FullscreenLoading()
      is DashboardState.Success -> DashboardContent(
        state = state,
        scrollConnection = scrollConnection,
        onNavigateToDate = onNavigateToDate,
        onNavigateToStatus = onNavigateToStatus,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
  state: DashboardState.Success,
  scrollConnection: NestedScrollConnection,
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      StyledTopAppBar(
        title = {
          Text(text = stringResource(R.string.dashboard_screen_title))
        },
      )
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
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
  }
}

@PreviewDynamicLightDark
@Composable
fun DashboardContentPreview() {
  PreviewTheme {
    Surface {
      DashboardContent(
        state = DashboardState.Success(
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
        scrollConnection = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
          exitDirection = FloatingToolbarExitDirection.Bottom,
        ),
        onNavigateToDate = {},
        onNavigateToStatus = {},
      )
    }
  }
}