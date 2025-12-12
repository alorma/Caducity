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
      ProductItem(
        product = product,
        shape = MaterialTheme.shapes.largeIncreased,
      )
    }
  }
}

@Composable
private fun ProductItem(
  product: ProductUiModel,
  shape: Shape,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    shape = shape,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {}
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = product.name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      if (product.description.isNotEmpty()) {
        Text(
          text = product.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      when (product) {
        is ProductUiModel.WithInstances -> {
          val instances = product.instances

          val statuses = instances.map { instance ->
            instance.status
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            statuses.forEachIndexed { index, status ->
              val colors = ExpirationColors.getSectionColors(status)

              val shape = if (statuses.size == 1) {
                MaterialTheme.shapes.small
              } else if (index == 0) {
                RoundedCornerShape(
                  topStart = MaterialTheme.shapes.small.topStart,
                  topEnd = CornerSize(0.dp),
                  bottomEnd = CornerSize(0.dp),
                  bottomStart = MaterialTheme.shapes.small.bottomStart,
                )
              } else if (index > 0 && index < (statuses.size - 1)) {
                RectangleShape
              } else {
                RoundedCornerShape(
                  topStart = CornerSize(0.dp),
                  topEnd = MaterialTheme.shapes.small.topStart,
                  bottomEnd = MaterialTheme.shapes.small.bottomStart,
                  bottomStart = CornerSize(0.dp),
                )
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(shape)
                  .background(colors.onContainer)
                  .padding(20.dp),
              )
            }
          }
        }

        is ProductUiModel.Empty -> {
          Text(
            text = "No active instances",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
