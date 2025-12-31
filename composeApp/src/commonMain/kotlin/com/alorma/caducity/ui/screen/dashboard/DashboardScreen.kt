package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
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
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_screen_title
import com.alorma.caducity.base.ui.components.StyledTopAppBar
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.components.DashboardSummaryCard
import com.alorma.caducity.ui.screen.dashboard.components.ProductsCalendar
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
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
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollConnection)
      .then(modifier),
    topBar = {
      StyledTopAppBar(
        title = {
          Text(text = stringResource(Res.string.dashboard_screen_title))
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
          )
        }
      }
    }
  }
}

