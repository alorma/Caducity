package com.alorma.caducity.ui.screen.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.domain.usecase.ProductsListFilter
import androidx.compose.ui.res.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Full screen version of Products List with top bar.
 * Use this for top-level navigation routes.
 */
@Suppress("ModifierReuse")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListScreen(
  filters: ProductsListFilter,
  onNavigateToProductDetail: (String) -> Unit,
  scrollConnection: NestedScrollConnection,
  modifier: Modifier = Modifier,
  viewModel: ProductsListViewModel = koinViewModel { parametersOf(filters) },
) {
  LaunchedEffect(filters) {
    viewModel.onFiltersUpdate(filters)
  }

  val state by viewModel.state.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .nestedScroll(scrollConnection),
    topBar = {
      StyledTopAppBar(
        title = { Text(stringResource(R.string.products_screen_title)) },
      )
    },
  ) { paddingValues ->
    ProductsListContent(
      state = state,
      onNavigateToProductDetail = onNavigateToProductDetail,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
    )
  }
}

/**
 * Bottom sheet version of Products List without top bar.
 * Use this for filtered views shown as bottom sheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListBottomSheet(
  filters: ProductsListFilter,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductsListViewModel = koinViewModel { parametersOf(filters) },
) {
  LaunchedEffect(filters) {
    viewModel.onFiltersUpdate(filters)
  }

  val state by viewModel.state.collectAsStateWithLifecycle()

  ProductsListContent(
    state = state,
    onNavigateToProductDetail = onNavigateToProductDetail,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}

@Composable
private fun ProductsListContent(
  state: ProductsListState,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    when (val currentState = state) {
      is ProductsListState.Loading -> ProductsListLoading()
      is ProductsListState.Empty -> ProductsListEmptyState(currentState)
      is ProductsListState.Success -> ProductsListSuccess(currentState, onNavigateToProductDetail)
    }
  }
}

@Composable
private fun ProductsListLoading() {
  Text(
    text = "Loading...",
    style = MaterialTheme.typography.bodyMedium,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(vertical = 32.dp),
  )
}

@Composable
private fun ProductsListEmptyState(state: ProductsListState.Empty) {
  when (val filter = state.filter) {
    ProductsListFilter.All -> {
      Text(text = "No products")
    }

    is ProductsListFilter.ByDate -> {
      Text(text = "No products for date ${filter.date}")
    }

    is ProductsListFilter.ByDateRange -> {
      Text(text = "No products for date range ${filter.startDate} - ${filter.endDate}")
    }

    is ProductsListFilter.ByStatus -> {
      Text(text = "No products for statuses: ${filter.statuses}")
    }
  }
}

@Composable
private fun ProductsListSuccess(
  state: ProductsListState.Success,
  onNavigateToProductDetail: (String) -> Unit
) {
  LazyColumn(
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(state.items, key = { it.id }) { product ->
      ProductsListItem(
        product = product,
        onClick = { onNavigateToProductDetail(product.id) },
      )
    }
  }
}
