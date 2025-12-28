package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_action_collapse
import caducity.composeapp.generated.resources.dashboard_action_expand
import caducity.composeapp.generated.resources.dashboard_clear_filters
import caducity.composeapp.generated.resources.dashboard_filter_expired
import caducity.composeapp.generated.resources.dashboard_filter_expiring_soon
import caducity.composeapp.generated.resources.dashboard_filter_fresh
import caducity.composeapp.generated.resources.dashboard_screen_title
import caducity.composeapp.generated.resources.dashboard_search_placeholder
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Close
import com.alorma.caducity.base.ui.icons.Search
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.ui.screen.dashboard.product.ProductItem
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DashboardScreen(
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
  showExpiringOnly: Boolean = false,
  viewModel: DashboardViewModel = koinViewModel { parametersOf(showExpiringOnly) }
) {
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()

  when (val state = dashboardState.value) {
    is DashboardState.Loading -> {
      Box(
        modifier = Modifier.fillMaxSize().then(modifier),
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
      onToggleExpand = viewModel::toggleExpanded,
      onSearchQueryChange = viewModel::updateSearchQuery,
      onStatusFiltersChange = viewModel::updateStatusFilters,
      onNavigateToProductDetail = onNavigateToProductDetail,
    )
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
  state: DashboardState.Success,
  onToggleExpand: (Boolean) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onStatusFiltersChange: (Set<InstanceStatus>) -> Unit,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(Res.string.dashboard_screen_title),
            style = MaterialTheme.typography.headlineMedium,
          )
        },
        actions = {
          TextButton(
            onClick = { onToggleExpand(!state.config.collapsed) },
          ) {
            Text(
              text = if (state.config.collapsed) {
                stringResource(Res.string.dashboard_action_expand)
              } else {
                stringResource(Res.string.dashboard_action_collapse)
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
      // Search bar
      OutlinedTextField(
        value = state.config.searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(stringResource(Res.string.dashboard_search_placeholder)) },
        leadingIcon = {
          Icon(
            imageVector = AppIcons.Search,
            contentDescription = null,
          )
        },
        trailingIcon = {
          if (state.config.searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(
                imageVector = AppIcons.Close,
                contentDescription = null,
              )
            }
          }
        },
        singleLine = true,
      )

      // Filter chips
      FlowRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilterChip(
          selected = state.config.statusFilters.contains(InstanceStatus.Fresh),
          onClick = {
            val newFilters = if (state.config.statusFilters.contains(InstanceStatus.Fresh)) {
              state.config.statusFilters - InstanceStatus.Fresh
            } else {
              state.config.statusFilters + InstanceStatus.Fresh
            }
            onStatusFiltersChange(newFilters)
          },
          label = { Text(stringResource(Res.string.dashboard_filter_fresh)) },
        )

        FilterChip(
          selected = state.config.statusFilters.contains(InstanceStatus.ExpiringSoon),
          onClick = {
            val newFilters = if (state.config.statusFilters.contains(InstanceStatus.ExpiringSoon)) {
              state.config.statusFilters - InstanceStatus.ExpiringSoon
            } else {
              state.config.statusFilters + InstanceStatus.ExpiringSoon
            }
            onStatusFiltersChange(newFilters)
          },
          label = { Text(stringResource(Res.string.dashboard_filter_expiring_soon)) },
        )

        FilterChip(
          selected = state.config.statusFilters.contains(InstanceStatus.Expired),
          onClick = {
            val newFilters = if (state.config.statusFilters.contains(InstanceStatus.Expired)) {
              state.config.statusFilters - InstanceStatus.Expired
            } else {
              state.config.statusFilters + InstanceStatus.Expired
            }
            onStatusFiltersChange(newFilters)
          },
          label = { Text(stringResource(Res.string.dashboard_filter_expired)) },
        )

        if (state.config.statusFilters.isNotEmpty()) {
          TextButton(
            onClick = { onStatusFiltersChange(emptySet()) },
          ) {
            Text(stringResource(Res.string.dashboard_clear_filters))
          }
        }
      }

      ProductsGrid(
        products = state.items,
        collapsed = state.config.collapsed,
        onNavigateToProductDetail = onNavigateToProductDetail,
      )
    }
  }
}

@Composable
private fun ProductsGrid(
  products: ImmutableList<ProductUiModel>,
  collapsed: Boolean,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().then(modifier),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(products) { product ->
      ProductItem(
        product = product,
        collapsed = collapsed,
        onClick = onNavigateToProductDetail,
      )
    }
  }
}
