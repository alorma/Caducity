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

  val state by viewModel.state.collectAsStateWithLifecycle()

  val products = when (state) {
    is ProductsListState.Success -> (state as ProductsListState.Success).items
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
      items(products, key = { it.id }) { product ->
        ProductsListItem(
          product = product,
          onClick = { onNavigateToProductDetail(product.id) },
        )
      }

      if (products.isEmpty()) {
        item {
          Text(
            text = "No products found",
            style = MaterialTheme.typography.bodyMedium,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 32.dp),
          )
        }
      }
    }
  }
}
