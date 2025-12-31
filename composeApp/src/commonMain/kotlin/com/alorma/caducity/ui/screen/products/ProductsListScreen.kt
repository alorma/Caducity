package com.alorma.caducity.ui.screen.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.base.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListScreen(
  filters: ProductsListFilter,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductsListViewModel = koinViewModel { parametersOf(filters) },
) {

  LaunchedEffect(filters) {
    viewModel.onFiltersUpdate(filters)
  }

  val state by viewModel.state.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .then(modifier),
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
