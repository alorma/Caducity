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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.CalendarCollapse
import com.alorma.caducity.base.ui.icons.CalendarExpand
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.components.DashboardSummaryCard
import com.alorma.caducity.ui.screen.dashboard.components.ProductsCalendar
import kotlinx.datetime.LocalDate
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DashboardScreen(
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
  showExpiringOnly: Boolean = false,
  viewModel: DashboardViewModel = koinViewModel { parametersOf(showExpiringOnly) }
) {
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()

  when (val state = dashboardState.value) {
    is DashboardState.Loading -> {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .then(modifier),
        contentAlignment = Alignment.Center,
      ) {
        LoadingIndicator(
          color = CaducityTheme.colorScheme.secondary,
          polygons = listOf(
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Cookie6Sided,
          ),
        )
      }
    }

    is DashboardState.Success -> DashboardContent(
      state = state,
      scrollConnection = scrollConnection,
      onNavigateToDate = onNavigateToDate,
      onNavigateToStatus = onNavigateToStatus,
      onToggleCalendarMode = { viewModel.toggleCalendarMode() },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
  state: DashboardState.Success,
  scrollConnection: NestedScrollConnection,
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  onToggleCalendarMode: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      StyledTopAppBar(
        title = {
          Text(text = stringResource(R.string.dashboard_screen_title))
        },
        actions = {
          IconButton(onClick = onToggleCalendarMode) {
            Icon(
              imageVector = when (state.config.calendarMode) {
                CalendarMode.MONTH -> AppIcons.CalendarCollapse
                CalendarMode.WEEK -> AppIcons.CalendarExpand
              },
              contentDescription = when (state.config.calendarMode) {
                CalendarMode.MONTH -> "Switch to week view"
                CalendarMode.WEEK -> "Switch to month view"
              }
            )
          }
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
        // Summary card showing up to 3 items: expired, expiring soon, fresh
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
          ProductsCalendar(
            calendarData = state.calendarData,
            onDateClick = onNavigateToDate,
            calendarMode = state.config.calendarMode,
          )
        }
      }
    }
  }
}

