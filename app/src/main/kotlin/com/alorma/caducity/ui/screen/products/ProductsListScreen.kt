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
    contentPadding = PaddingValues(bottom = 16.dp),
  ) {
    state.items.forEachIndexed { index, product ->
      item(
        key = "product-${product.id}-title",
        contentType = "product-title",
      ) {
        ProductTitle(
          name = product.name,
          isFirst = index == 0,
        )
      }

      when (product) {
        is ProductListUiModel.Empty -> {
          item(
            key = "product-${product.id}-empty",
            contentType = "product-empty",
          ) {
            ProductEmptyState()
          }
        }

        is ProductListUiModel.WithContent -> {
          itemsIndexed(
            items = product.variants,
            key = { _, variant -> "product-${product.id}-variant-${variant.id}" },
            contentType = { _, _ -> "product-variant" },
          ) { index, variant ->
            ProductVariantCard(
              variant = variant,
              shapePosition = product.variants.calculateShape(index),
            )
          }

          if (product.variants.isNotEmpty()) {
            item {
              Spacer(modifier = Modifier.height(8.dp))
            }
          }

          itemsIndexed(
            items = product.standaloneInstances,
            key = { _, instance -> "product-${product.id}-instance-${instance.id}" },
            contentType = { _, _ -> "product-instance" },
          ) { index, instance ->
            ProductInstanceCard(
              instance = instance,
              shapePosition = product.standaloneInstances.calculateShape(index),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ProductTitle(
  name: String,
  isFirst: Boolean,
  modifier: Modifier = Modifier,
) {
  Text(
    modifier = modifier.padding(
      top = if (isFirst) 0.dp else 24.dp,
      bottom = 8.dp
    ),
    text = name,
    style = MaterialTheme.typography.titleMedium,
    color = CaducityTheme.colorScheme.onSurface,
  )
}

@Composable
private fun ProductEmptyState(
  modifier: Modifier = Modifier,
) {
  Text(
    modifier = modifier.padding(vertical = 4.dp),
    text = "No instances",
    style = MaterialTheme.typography.bodySmall,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun ProductVariantCard(
  variant: ProductInstanceVariant,
  shapePosition: com.alorma.caducity.ui.components.shape.ShapePosition,
  modifier: Modifier = Modifier,
) {
  val totalCount = remember(variant.id) {
    variant.statusGroups.sumOf { it.count } + variant.frozenCount
  }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = shapePosition.toVerticalShape(),
    color = CaducityTheme.colorScheme.surfaceContainer,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
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
        )
      }
    }
  }
}

@Composable
private fun ProductInstanceCard(
  instance: ProductListStandaloneInstance,
  shapePosition: com.alorma.caducity.ui.components.shape.ShapePosition,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = shapePosition.toVerticalShape(),
    color = CaducityTheme.colorScheme.surfaceContainer,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
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
