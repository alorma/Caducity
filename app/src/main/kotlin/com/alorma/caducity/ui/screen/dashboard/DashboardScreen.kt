package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.outlined.Settings
import com.alorma.caducity.base.ui.icons.outlined.Sparkle
import com.alorma.caducity.config.remoteconfig.rememberRemoteConfig
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.feature.ai.AiFeatureConfig
import com.alorma.caducity.feature.tracking.DashboardScreen as DashboardScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.dashboard.components.DashboardSuccessContentList
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
  onNavigateToCreateProduct: () -> Unit,
  onNavigateToCategory: (String) -> Unit,
  onNavigateToStatus: (ItemStatus) -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToAiAssistant: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DashboardViewModel = koinViewModel(),
) {
  TrackScreen(screen = DashboardScreenEvent())
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()
  val snackbarHostState = rememberAppSnackbarState()

  // Handle navigation side effects
  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { navigationEffect ->
      when (navigationEffect) {
        DashboardNavigationSideEffect.NavigateToCreateCategory -> onNavigateToCreateProduct()
        is DashboardNavigationSideEffect.NavigateToCategory -> onNavigateToCategory(navigationEffect.categoryId)
        is DashboardNavigationSideEffect.NavigateToFilteredItems -> onNavigateToStatus(navigationEffect.status)
        DashboardNavigationSideEffect.NavigateToSettings -> onNavigateToSettings()
        DashboardNavigationSideEffect.NavigateToAiAssistant -> onNavigateToAiAssistant()
      }
    }
  }

  Box(modifier) {
    DashboardContent(
      state = dashboardState.value,
      onNavigateToCreateProduct = {
        viewModel.navigate(DashboardNavigation.CreateCategory)
      },
      onNavigateToCategory = { categoryId, source ->
        viewModel.navigate(DashboardNavigation.Category(categoryId, source))
      },
      onNavigateToStatus = { status ->
        viewModel.navigate(DashboardNavigation.FilteredItems(status))
      },
      onNavigateToSettings = {
        viewModel.navigate(DashboardNavigation.Settings)
      },
      onNavigateToAiAssistant = {
        viewModel.navigate(DashboardNavigation.AiAssistant)
      },
      snackbarHostState = snackbarHostState,
    )
  }
}

@Composable
private fun DashboardContent(
  state: DashboardState,
  onNavigateToCreateProduct: () -> Unit,
  onNavigateToCategory: (String, String) -> Unit,
  onNavigateToStatus: (ItemStatus) -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToAiAssistant: () -> Unit,
  snackbarHostState: AppSnackbarState,
) {
  val lazyListState = rememberLazyListState()
  val aiFeatureConfig = rememberRemoteConfig<AiFeatureConfig>()

  AppScaffold(
    topBar = {
      StyledTopAppBar(
        title = { Text(text = stringResource(R.string.dashboard_screen_title)) },
        actions = {
          if (aiFeatureConfig.isEnabled()) {
            IconButton(onClick = onNavigateToAiAssistant) {
              Icon(
                imageVector = AppIcons.Outlined.Sparkle,
                contentDescription = null,
              )
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
    floatingActionButton = {
      val expanded =
        remember {
          derivedStateOf {
            lazyListState.firstVisibleItemScrollOffset > 0
          }
        }
      ExtendedFloatingActionButton(
        expanded = !expanded.value,
        onClick = onNavigateToCreateProduct,
        text = { Text(stringResource(R.string.dashboard_add)) },
        icon = {
          Icon(
            imageVector = AppIcons.Add,
            contentDescription = null,
          )
        },
      )
    },
    snackbarState = snackbarHostState,
  ) { paddingValues ->
    when (state) {
      is DashboardState.Loading -> DashboardLoadingContent()
      is DashboardState.Success ->
        DashboardSuccessContent(
          modifier = Modifier.padding(paddingValues),
          state = state,
          lazyListState = lazyListState,
          onNavigateToCategory = onNavigateToCategory,
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
  lazyListState: LazyListState,
  onNavigateToCategory: (String, String) -> Unit,
  onNavigateToStatus: (ItemStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .then(modifier),
  ) {
    DashboardSuccessContentList(
      state = state,
      lazyListState = lazyListState,
      onNavigateToCategory = onNavigateToCategory,
      onNavigateToStatus = onNavigateToStatus,
    )
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
fun DashboardSuccessContentPreview(
  @PreviewParameter(provider = DashboardPreviewProvider::class) state: DashboardState,
) {
  PreviewTheme {
    Surface {
      DashboardContent(
        state = state,
        onNavigateToCreateProduct = {},
        onNavigateToCategory = { _, _ -> },
        onNavigateToStatus = {},
        onNavigateToSettings = {},
        onNavigateToAiAssistant = {},
        snackbarHostState = rememberAppSnackbarState(),
      )
    }
  }
}
