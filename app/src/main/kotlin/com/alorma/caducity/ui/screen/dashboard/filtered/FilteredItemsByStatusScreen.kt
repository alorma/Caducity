package com.alorma.caducity.ui.screen.dashboard.filtered

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.ui.adaptive.rememberIsExpanded
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FilteredItemsByStatusScreen(
  status: ItemStatus,
  modifier: Modifier = Modifier,
  viewModel: FilteredItemsByStatusViewModel = koinViewModel {
    parametersOf(status)
  },
  onNavigateToCategory: (String) -> Unit = {},
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbarState = rememberAppSnackbarState()
  val bottomSheetState = rememberAppBottomSheetState()

  SideEffectHandler(
    viewModel = viewModel,
    bottomSheetState = bottomSheetState,
    snackbarState = snackbarState,
  )

  AppScaffold(
    modifier = modifier,
    topBar = {
      StyledTopAppBar(
        title = {
          Text(
            text = stringResource(
              R.string.filtered_items_by_status_title,
              ExpirationDefaults.getTitle(status)
            )
          )
        },
        navigationIcon = { NavigationIcon() },
      )
    },
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
  ) { paddingValues ->
    when (val currentState = state) {
      is FilteredItemsByStatusState.Loading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
          contentAlignment = Alignment.Center,
        ) {
          FullscreenLoading()
        }
      }

      is FilteredItemsByStatusState.Success -> {
        FilteredItemsContent(
          modifier = Modifier.padding(paddingValues),
          categories = currentState.categories,
          status = status,
          onProductClick = viewModel::onProductClick,
          onCategoryClick = onNavigateToCategory,
        )
      }

      is FilteredItemsByStatusState.Empty -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(R.string.filtered_items_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      is FilteredItemsByStatusState.Error -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = currentState.message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}

@Composable
private fun FilteredItemsContent(
  categories: List<CategoryWithItems>,
  status: ItemStatus,
  onProductClick: (String, List<Item>) -> Unit,
  onCategoryClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isExpanded = rememberIsExpanded()

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Box(
      modifier = if (isExpanded) {
        Modifier.widthIn(max = 800.dp).fillMaxWidth()
      } else {
        Modifier.fillMaxWidth()
      }
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        items(
          items = categories,
          key = { it.category.id },
        ) { categoryWithItems ->
          CategoryItemsCard(
            categoryWithItems = categoryWithItems,
            status = status,
            onProductClick = onProductClick,
            onCategoryClick = onCategoryClick,
          )
        }
      }
    }
  }
}

@Composable
private fun CategoryItemsCard(
  categoryWithItems: CategoryWithItems,
  status: ItemStatus,
  onProductClick: (String, List<Item>) -> Unit,
  onCategoryClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = ExpirationDefaults.getSoftColors(status)
  val vibrantColors = ExpirationDefaults.getVibrantColors(status)

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Category header
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onCategoryClick(categoryWithItems.category.id) },
      shape = MaterialTheme.shapes.small,
      color = colors.container,
      contentColor = colors.onContainer,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          modifier = Modifier.weight(1f),
          text = categoryWithItems.category.name,
          style = MaterialTheme.typography.titleMedium,
          color = colors.onContainer,
        )

        // Filtered items count badge
        val filteredItemsCount = categoryWithItems.allItems.size
        Text(
          modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(vibrantColors.container)
            .padding(horizontal = 8.dp, vertical = 4.dp),
          text = filteredItemsCount.toString(),
          color = vibrantColors.onContainer,
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }

    // Items grouped by product
    categoryWithItems.products.forEach { categoryProduct ->
      ProductItemsGroup(
        productName = categoryProduct.product.name,
        itemCount = categoryProduct.items.size,
        onClick = {
          onProductClick(categoryProduct.product.name, categoryProduct.items)
        }
      )
    }

    // Standalone items
    if (categoryWithItems.standaloneItems.isNotEmpty()) {
      val otherLabel = stringResource(R.string.category_detail_product_other)
      ProductItemsGroup(
        productName = otherLabel,
        itemCount = categoryWithItems.standaloneItems.size,
        onClick = {
          onProductClick(otherLabel, categoryWithItems.standaloneItems)
        }
      )
    }
  }
}

@Composable
private fun ProductItemsGroup(
  productName: String,
  itemCount: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(CaducityTheme.shapes.small)
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = productName,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Text(
      text = stringResource(R.string.filtered_items_count, itemCount),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
