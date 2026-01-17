package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.outlined.Settings
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarHostState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.dashboard.components.DashboardSuccessContentList
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
  onNavigateToCreateProduct: () -> Unit,
  onNavigateToProduct: (String) -> Unit,
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  onNavigateToSettings: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DashboardViewModel = koinViewModel(),
) {
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()
  val aiGenerationState = viewModel.aiGenerationState.collectAsStateWithLifecycle()

  val showAISheet = remember { mutableStateOf(false) }
  val showFABMenu = remember { mutableStateOf(false) }
  val snackbarHostState = rememberAppSnackbarHostState()

  // Get strings outside of LaunchedEffect
  val successMessageFormat = stringResource(R.string.dashboard_ai_success)

  // Handle AI generation completion
  LaunchedEffect(aiGenerationState.value.completedResult) {
    aiGenerationState.value.completedResult?.let { result ->
      showAISheet.value = false
      val message = successMessageFormat.format(
        result.productsCreated,
        result.variantsCreated,
        result.instancesCreated
      )
      snackbarHostState.showSnackbar(
        message = message,
        duration = SnackbarDuration.Short
      )
      showAISheet.value = false
      viewModel.resetAIState()
    }
  }

  // Handle AI generation errors
  LaunchedEffect(aiGenerationState.value.error) {
    aiGenerationState.value.error?.let { error ->
      snackbarHostState.showSnackbar(
        message = error,
        duration = SnackbarDuration.Long
      )
      viewModel.dismissAIError()
    }
  }

  // Show AI input sheet
  if (showAISheet.value) {
    DashboardAIInputSheet(
      onDismiss = { showAISheet.value = false },
      onGenerate = { prompt ->
        viewModel.onGenerateFromPrompt(prompt)
      },
      isGenerating = aiGenerationState.value.isGenerating
    )
  }

  // Show AI review sheet when matches are found
  aiGenerationState.value.awaitingReview?.let { matchingResults ->
    DashboardAIReviewSheet(
      matchingResults = matchingResults,
      onDismiss = {
        showAISheet.value = false
        viewModel.resetAIState()
      },
      onConfirm = {
        viewModel.onConfirmMatches(matchingResults)
      }
    )
  }

  Box(modifier) {
    DashboardContent(
      state = dashboardState.value,
      onNavigateToCreateProduct = onNavigateToCreateProduct,
      onNavigateToDate = onNavigateToDate,
      onNavigateToProduct = onNavigateToProduct,
      onNavigateToStatus = onNavigateToStatus,
      onNavigateToSettings = onNavigateToSettings,
      onOpenAISheet = { showAISheet.value = true },
      snackbarHostState = snackbarHostState,
    )
  }
}

@Composable
private fun DashboardContent(
  state: DashboardState,
  onNavigateToCreateProduct: () -> Unit,
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToProduct: (String) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  onNavigateToSettings: () -> Unit,
  onOpenAISheet: () -> Unit,
  snackbarHostState: com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHostState,
) {
  var showFABMenu by remember { mutableStateOf(false) }

  AppScaffold(
    topBar = {
      StyledTopAppBar(
        title = { Text(text = stringResource(R.string.dashboard_screen_title)) },
        actions = {
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
    floatingActionButton = {
      if (showFABMenu) {
        Column {
          // Manual option
          ExtendedFloatingActionButton(
            onClick = {
              onNavigateToCreateProduct()
              showFABMenu = false
            },
            text = { Text(stringResource(R.string.dashboard_fab_manual)) },
            icon = {
              Icon(
                imageVector = AppIcons.Add,
                contentDescription = null,
              )
            }
          )
          // AI option
          ExtendedFloatingActionButton(
            onClick = {
              onOpenAISheet()
              showFABMenu = false
            },
            text = { Text(stringResource(R.string.dashboard_fab_ai)) },
            icon = {
              Icon(
                imageVector = AppIcons.Add,
                contentDescription = null,
              )
            },
            modifier = Modifier.padding(top = 8.dp)
          )
        }
      } else {
        ExtendedFloatingActionButton(
          onClick = { showFABMenu = true },
          text = { Text("Add") },
          icon = {
            Icon(
              imageVector = AppIcons.Add,
              contentDescription = null,
            )
          }
        )
      }
    },
    snackbarState = snackbarHostState,
  ) { paddingValues ->
    when (state) {
      is DashboardState.Loading -> DashboardLoadingContent()
      is DashboardState.Success -> DashboardSuccessContent(
        modifier = Modifier.padding(paddingValues),
        state = state,
        onNavigateToDate = onNavigateToDate,
        onNavigateToProduct = onNavigateToProduct,
        onNavigateToStatus = onNavigateToStatus,
      )
    }
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
  onNavigateToDate: (LocalDate) -> Unit,
  onNavigateToProduct: (String) -> Unit,
  onNavigateToStatus: (InstanceStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
  ) {
    DashboardSuccessContentList(
      state = state,
      onNavigateToProduct = onNavigateToProduct,
      onNavigateToStatus = onNavigateToStatus,
      onNavigateToDate = onNavigateToDate,
    )
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
        onNavigateToCreateProduct = {},
        onNavigateToDate = {},
        onNavigateToProduct = {},
        onNavigateToStatus = {},
        onNavigateToSettings = {},
        onOpenAISheet = {},
        snackbarHostState = rememberAppSnackbarHostState(),
      )
    }
  }
}