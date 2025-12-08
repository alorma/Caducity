package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.ui.adaptive.isWidthCompact
import com.alorma.caducity.ui.icons.AppIcons
import com.alorma.caducity.ui.icons.ArrowUp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel = koinViewModel()
) {
  val dashboardState by viewModel.state.collectAsStateWithLifecycle()
  var expandedSectionIndex by remember { mutableStateOf<Int?>(null) }
  val isCompact = isWidthCompact()
  val columnsPerRow = if (isCompact) 2 else 3

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Dashboard",
      style = MaterialTheme.typography.headlineMedium,
    )

    when (val state = dashboardState) {
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

      is DashboardState.Success -> {
        DashboardGrid(
          sections = state.sections,
          expandedSectionIndex = expandedSectionIndex,
          columnsPerRow = columnsPerRow,
          onSectionClick = { index ->
            expandedSectionIndex = if (expandedSectionIndex == index) null else index
          }
        )
      }
    }
  }
}

@Composable
private fun DashboardGrid(
  sections: List<DashboardSection>,
  expandedSectionIndex: Int?,
  columnsPerRow: Int,
  onSectionClick: (Int) -> Unit,
) {
  LazyVerticalGrid(
    columns = GridCells.Fixed(columnsPerRow),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    sections.forEachIndexed { index, section ->
      val isExpanded = expandedSectionIndex == index

      if (isExpanded) {
        // Full-width expanded card spans all columns
        item(span = { GridItemSpan(columnsPerRow) }) {
          AnimatedVisibility(
            visible = true,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            DashboardExpandedCard(
              sectionType = section.type,
              title = stringResource(section.title),
              value = section.itemCount.toString(),
              products = section.products,
              onCollapse = { onSectionClick(index) }
            )
          }
        }
      } else {
        // Compact card in grid
        item {
          AnimatedVisibility(
            visible = true,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            DashboardCompactCard(
              sectionType = section.type,
              title = stringResource(section.title),
              value = section.itemCount.toString(),
              onClick = { onSectionClick(index) },
              modifier = Modifier
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DashboardCompactCard(
  sectionType: SectionType,
  title: String,
  value: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val sectionColors = DashboardSectionColors.getSectionColors(sectionType)

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = sectionColors.container,
    contentColor = sectionColors.onContainer,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.displayMedium,
      )
    }
  }
}

@Composable
private fun DashboardExpandedCard(
  sectionType: SectionType,
  title: String,
  value: String,
  products: List<ProductUiModel>,
  onCollapse: () -> Unit,
) {
  val sectionColors = DashboardSectionColors.getSectionColors(sectionType)

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = sectionColors.container,
    contentColor = sectionColors.onContainer,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onCollapse)
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
          )
          Text(
            text = "$value items",
            style = MaterialTheme.typography.bodyMedium,
          )
        }

        Icon(
          imageVector = AppIcons.ArrowUp,
          contentDescription = "Collapse",
        )
      }

      HorizontalDivider()

      if (products.isEmpty()) {
        Text(
          text = "No products in this category",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(16.dp),
        )
      } else {
        products.forEach { product ->
          ProductCard(product = product)
          if (product != products.last()) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun ProductCard(
  product: ProductUiModel,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
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

    Text(
      text = "${product.instances.size} instance${if (product.instances.size != 1) "s" else ""}",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.secondary,
    )

    Column(
      modifier = Modifier.padding(start = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      product.instances.forEach { instance ->
        ProductInstanceItem(instance = instance)
      }
    }
  }
}

@Composable
private fun ProductInstanceItem(
  instance: ProductInstanceUiModel,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = "ID: ${instance.id.take(8)}...",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "Purchased: ${instance.purchaseDate}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Text(
      text = "Expires: ${instance.expirationDate}",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
