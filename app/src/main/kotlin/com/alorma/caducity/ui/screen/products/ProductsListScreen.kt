package com.alorma.caducity.ui.screen.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.domain.usecase.ProductsListFilter
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.loading.WavyLoadingIndicator
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.calculateShape
import com.alorma.caducity.ui.components.shape.toVerticalShape
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.products.components.StatusBarsRow
import com.alorma.caducity.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
  val state by viewModel.state.collectAsStateWithLifecycle()

  AppScaffold(
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
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      loading = { FullscreenLoading() },
      state = state,
      onNavigateToProductDetail = onNavigateToProductDetail,
    )
  }
}

@Composable
private fun ProductsListContent(
  state: ProductsListState,
  onNavigateToProductDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
  loading: @Composable () -> Unit = { ProductsListLoading() },
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    when (state) {
      is ProductsListState.Loading -> loading()
      is ProductsListState.Empty -> ProductsListEmptyState(state)
      is ProductsListState.Success -> ProductsListSuccess(state, onNavigateToProductDetail)
    }
  }
}

@Composable
private fun ProductsListLoading() {
  Box(
    modifier = Modifier
      .wrapContentHeight()
      .fillMaxWidth()
      .padding(vertical = 64.dp),
    contentAlignment = Alignment.Center,
  ) {
    WavyLoadingIndicator()
  }
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
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    state.items.forEachIndexed { index, product ->
      item(
        key = "product-${product.id}-title",
        contentType = "product-title",
      ) {
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleMedium,
          color = CaducityTheme.colorScheme.onSurface,
        )
      }

      when (product) {
        is ProductListUiModel.Empty -> {
          item(
            key = "product-${product.id}-empty",
            contentType = "product-empty",
          ) {
            Text(
              modifier = Modifier.padding(vertical = 4.dp),
              text = "No instances",
              style = MaterialTheme.typography.bodySmall,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        is ProductListUiModel.WithContent -> {
          itemsIndexed(
            items = product.variants,
            key = { index, variant -> "product-${product.id}-variant-${variant.id}" },
            contentType = { _, _ -> "product-variant" },
          ) { index, variant ->

            val shapePosition = product.variants.calculateShape(index)

            val totalCount = remember(variant.id) {
              variant.statusGroups.sumOf { it.count } + variant.frozenCount
            }

            Surface(
              shape = shapePosition.toVerticalShape(),
              color = CaducityTheme.colorScheme.surfaceContainer,
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
              ) {
                Text(
                  text = "—\t${variant.name} ($totalCount)",
                  style = MaterialTheme.typography.labelLarge,
                  color = CaducityTheme.colorScheme.onSurface,
                )
                StatusBarsRow(statusGroups = variant.statusGroups)
                if (variant.frozenCount > 0) {
                  Text(
                    text = stringResource(R.string.products_list_items_frozen, variant.frozenCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = CaducityTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                  )
                }
              }
            }
          }

          items(
            items = product.standaloneInstances,
            key = { instance -> "product-${product.id}-instance-${instance.id}" },
            contentType = { "product-variant" },
          ) { instance ->
            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "—\t${instance.name}",
                style = MaterialTheme.typography.labelLarge,
                color = CaducityTheme.colorScheme.onSurface,
              )
              StatusBadge(instance.status)
            }
          }
        }
      }

      if (index < state.items.lastIndex) {
        item {
          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }
  }
}
