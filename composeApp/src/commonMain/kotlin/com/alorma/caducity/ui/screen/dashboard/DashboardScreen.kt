package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_screen_title
import com.alorma.caducity.ui.screen.dashboard.product.ProductItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel = koinViewModel()
) {
  val dashboardState = viewModel.state.collectAsStateWithLifecycle()

  when (val state = dashboardState.value) {
    is DashboardState.Loading -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        LoadingIndicator(
          color = MaterialTheme.colorScheme.secondary,
          polygons = listOf(
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Cookie6Sided,
          ),
        )
      }
    }

    is DashboardState.Success -> DashboardContent(state)
  }
}

@Composable
fun DashboardContent(state: DashboardState.Success) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(Res.string.dashboard_screen_title)) },
      )
    },
  ) { paddingValues ->

    val adaptativeInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)
    val windowSizeClass = adaptativeInfo.windowSizeClass

    val isMedium = windowSizeClass.isWidthAtLeastBreakpoint(
      WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    )
    val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(
      WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
    )

    ProductsGrid(
      modifier = Modifier.padding(paddingValues),
      products = state.items,
      gridCells = if (isExpanded) {
        GridCells.FixedSize(320.dp)
      } else if (isMedium) {
        GridCells.Fixed(2)
      } else {
        GridCells.Fixed(1)
      }
    )
  }
}

@Composable
private fun ProductsGrid(
  products: List<ProductUiModel>,
  gridCells: GridCells,
  modifier: Modifier = Modifier,
) {
  LazyVerticalGrid(
    modifier = Modifier.fillMaxSize().then(modifier),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    columns = gridCells,
  ) {
    items(products) { product ->
      ProductItem(product = product)
    }
  }
}
