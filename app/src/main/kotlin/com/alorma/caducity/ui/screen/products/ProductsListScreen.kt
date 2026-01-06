package com.alorma.caducity.ui.screen.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.domain.usecase.ProductsListFilter
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.loading.WavyLoadingIndicator
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toVerticalShape
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
  val state by viewModel.state.collectAsStateWithLifecycle()

  ProductsListContent(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    loading = { ProductsListLoading() },
    state = state,
    onNavigateToProductDetail = onNavigateToProductDetail,
  )
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
    when (val currentState = state) {
      is ProductsListState.Loading -> loading()
      is ProductsListState.Empty -> ProductsListEmptyState(currentState)
      is ProductsListState.Success -> ProductsListSuccess(currentState, onNavigateToProductDetail)
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
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    itemsIndexed(state.items, key = { index, product -> product.id }) { index, product ->

      val shape = when {
        state.items.size == 1 -> ShapePosition.Single
        index == 0 -> ShapePosition.Start
        index == state.items.lastIndex -> ShapePosition.End
        else -> ShapePosition.Middle
      }

      Surface(
        shape = shape.toVerticalShape(),
        tonalElevation = 4.dp,
      ) {
        ProductsListItem(
          product = product,
          onClick = { onNavigateToProductDetail(product.id) },
        )
      }
    }
  }
}
