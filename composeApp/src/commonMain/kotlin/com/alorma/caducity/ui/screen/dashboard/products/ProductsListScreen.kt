package com.alorma.caducity.ui.screen.dashboard.products

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.ui.screen.dashboard.components.ProductItem
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListScreen(
  filters: ProductsListFilter,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductsListViewModel = koinViewModel(),
) {

  LaunchedEffect(filters) {
    viewModel.onFiltersUpdate(filters)
  }

  val state = viewModel.state.collectAsStateWithLifecycle()

  val productsForDate = when (val currentState = state.value) {
    is DateProductsState.Success -> currentState.items
    else -> emptyList()
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .then(modifier),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {

    LazyColumn(
      contentPadding = PaddingValues(bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(productsForDate, key = { it.id }) { product ->
        ProductItem(
          product = product,
          onClick = { onNavigateToProductDetail(product.id) },
        )
      }

      if (productsForDate.isEmpty()) {
        item {
          Text(
            text = "No products expiring on this date",
            style = MaterialTheme.typography.bodyMedium,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 32.dp),
          )
        }
      }
    }
  }
}
