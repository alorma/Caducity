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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.outlined.Calendar
import com.alorma.caducity.base.ui.icons.outlined.ListMode
import com.alorma.caducity.base.ui.icons.outlined.Settings
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.ui.components.calendar.CaducityMonthCalendar
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.screen.dashboard.components.DashboardSummaryCard
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  onNavigateToSettings: () -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
  viewModel: DashboardViewModel = koinViewModel(),
) {
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()

  Box(modifier) {
    DashboardContent(
      state = dashboardState.value,
      scrollConnection = scrollConnection,
      onNavigateToDate = onNavigateToDate,
      onNavigateToStatus = onNavigateToStatus,
      onNavigateToSettings = onNavigateToSettings,
      onChangeDashboardMode = viewModel::changeDashboardMode,
    )
  }
}

@Composable
private fun DashboardContent(
  state: DashboardState,
  scrollConnection: NestedScrollConnection,
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  onNavigateToSettings: () -> Unit,
  onChangeDashboardMode: (DashboardMode) -> Unit,
) {
  when (state) {
    is DashboardState.Loading -> DashboardLoadingContent()
    is DashboardState.Success -> DashboardSuccessContent(
      state = state,
      scrollConnection = scrollConnection,
      onNavigateToDate = onNavigateToDate,
      onNavigateToStatus = onNavigateToStatus,
      onNavigateToSettings = onNavigateToSettings,
      onChangeDashboardMode = onChangeDashboardMode,
    )
  }
}

@Composable
private fun DashboardLoadingContent() {
  FullscreenLoading()
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardSuccessContent(
  state: DashboardState.Success,
  scrollConnection: NestedScrollConnection,
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  onNavigateToSettings: () -> Unit,
  onChangeDashboardMode: (DashboardMode) -> Unit,
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
        actions = {
          when (state.data) {
            is DashboardModeState.Unified -> {
              IconButton(
                onClick = { onChangeDashboardMode(DashboardMode.PerProduct) },
              ) {
                Icon(
                  imageVector = AppIcons.Outlined.ListMode,
                  contentDescription = null,
                )
              }
            }

            is DashboardModeState.PerProduct -> {
              IconButton(
                onClick = { onChangeDashboardMode(DashboardMode.Unified) },
              ) {
                Icon(
                  imageVector = AppIcons.Outlined.Calendar,
                  contentDescription = null,
                )
              }
            }
          }


          IconButton(
            onClick = onNavigateToSettings,
          ) {
            Icon(
              imageVector = AppIcons.Outlined.Settings,
              contentDescription = null,
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

        item(
          key = "summary",
          contentType = "summary"
        ) {
          val summary = when (val mode = state.data) {
            is DashboardModeState.Unified -> mode.summary
            is DashboardModeState.PerProduct -> mode.summary
          }
          DashboardSummaryCard(
            summary = summary,
            onStatusClick = { status -> onNavigateToStatus(status) },
          )
        }

        item(contentType = "calendar") {
          val calendarState = when (val mode = state.data) {
            is DashboardModeState.Unified -> mode.calendarState
            is DashboardModeState.PerProduct -> mode.calendarState
          }
          CaducityMonthCalendar(
            calendarState = calendarState,
            onDateClick = onNavigateToDate,
          )
        }
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
fun DashboardSuccessContentPreview(
  @PreviewParameter(provider = DashboardPreviewProvider::class) state: DashboardState,
) {
  PreviewTheme {
    Surface {
      DashboardContent(
        state = state,
        scrollConnection = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
          exitDirection = FloatingToolbarExitDirection.Bottom,
        ),
        onNavigateToDate = {},
        onNavigateToStatus = {},
        onNavigateToSettings = {},
        onChangeDashboardMode = {},
      )
    }
  }
}