package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ProductsGrid(
  products: ImmutableList<ProductUiModel>,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(products) { product ->
      ProductItem(
        product = product,
        onClick = onNavigateToProductDetail,
      )
    }
  }
}
