package com.alorma.caducity.ui.screen.category.detail.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.adaptive.rememberGridColumns
import com.alorma.caducity.ui.components.calendar.today
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.screen.category.detail.CategoryProductTabUiModel
import com.alorma.caducity.ui.screen.category.detail.DateItemsUiModel
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.now
import kotlin.random.Random
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductTabContent(
  productTab: CategoryProductTabUiModel,
  onNavigateToAddItem: (categoryId: String, productId: String?) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductPageViewModel = koinViewModel(key = productTab.asKey()) {
    parametersOf(productTab)
  }
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  // Initialize feedback states
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()
  val bottomSheetState = rememberAppBottomSheetState()

  // Side effect handler with explicit states
  ProductPageSideEffectHandler(
    viewModel = viewModel,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
    onNavigateToAddItem = onNavigateToAddItem,
  )

  // Wrap with AppScaffold
  AppScaffold(
    modifier = modifier,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
    floatingActionButton = {
      // Calculate if there are any items in the product
      val hasItems = when (val currentState = state) {
        is ProductPageState.Success -> {
          currentState.datedItemsGroups.isNotEmpty() ||
            currentState.frozenItems.isNotEmpty() ||
            currentState.consumedItems.isNotEmpty()
        }

        else -> false
      }

      ProductActionsSplitButton(
        onAddItem = viewModel::onAddItemClick,
        onClearItems = viewModel::onClearProductItemsClick,
        onDeleteProduct = if (productTab.id != null) {
          { viewModel.onDeleteProductClick() }
        } else null,
        hasItems = hasItems,
      )
    }
  ) { paddingValues ->
    ProductTabContentPage(
      modifier = Modifier.padding(paddingValues),
      state = state,
      onItemClick = viewModel::onItemClick,
    )
  }
}

@Composable
private fun ProductTabContentPage(
  state: ProductPageState,
  onItemClick: (ItemDetailUiModel) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (state) {
    is ProductPageState.Loading -> {
      Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        FullscreenLoading()
      }
    }

    is ProductPageState.Success -> {
      // Check if there are no items at all
      if (state.datedItemsGroups.isEmpty() &&
        state.frozenItems.isEmpty() &&
        state.consumedItems.isEmpty()
      ) {
        // Show empty state
        Box(
          modifier = modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(R.string.category_detail_product_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      } else {
        LazyColumn(
          modifier = modifier.fillMaxSize(),
          contentPadding = PaddingValues(
            top = 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 80.dp,
          ),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Show dated items groups
          state.datedItemsGroups.forEach { datedItems ->
            item {
              SectionHeader(
                status = datedItems.status,
                date = datedItems.text,
                title = ExpirationDefaults.getTitle(datedItems.status),
                count = datedItems.items.size,
              )
            }
            item {
              StatusGroupCard(
                items = datedItems.items,
                onItemClick = onItemClick,
              )
            }
          }

          // Frozen items
          if (state.frozenItems.isNotEmpty()) {
            item {
              SectionHeader(
                status = ItemStatus.Frozen,
                title = stringResource(R.string.category_detail_section_frozen),
                count = state.frozenItems.size,
              )
            }
            item {
              StatusGroupCard(
                items = state.frozenItems,
                onItemClick = onItemClick,
              )
            }
          }

          // Consumed items
          if (state.consumedItems.isNotEmpty()) {
            item {
              SectionHeader(
                status = ItemStatus.Consumed,
                title = stringResource(R.string.category_detail_section_consumed),
                count = state.consumedItems.size,
              )
            }
            item {
              StatusGroupCard(
                items = state.consumedItems,
                onItemClick = onItemClick,
              )
            }
          }
        }
      }
    }

    is ProductPageState.Error -> {
      Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = state.message,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@Composable
private fun SectionHeader(
  status: ItemStatus,
  title: String,
  count: Int,
  modifier: Modifier = Modifier,
  date: String? = null,
) {
  val toneColors = ExpirationDefaults.getColors(status)
  val inverseToneColors = ExpirationDefaults.getInverseColors(status)

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    shape = CaducityTheme.shapes.small,
    color = toneColors.container,
    contentColor = toneColors.onContainer,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          modifier = Modifier.weight(1f),
          text = title,
          style = CaducityTheme.typography.titleMedium,
          color = toneColors.onContainer,
        )

        Text(
          modifier = Modifier
            .clip(CaducityTheme.shapes.extraSmall)
            .background(inverseToneColors.container)
            .padding(horizontal = 8.dp, vertical = 4.dp),
          text = count.toString(),
          color = inverseToneColors.onContainer,
          style = CaducityTheme.typography.labelMedium,
        )
      }

      if (date != null) {
        Text(
          text = date,
          style = CaducityTheme.typography.labelSmallEmphasized,
          color = toneColors.onContainer,
        )
      }
    }
  }
}

@Composable
private fun StatusGroupCard(
  items: ImmutableList<ItemDetailUiModel>,
  onItemClick: (ItemDetailUiModel) -> Unit,
) {
  val gridColumns = rememberGridColumns()

  // Show instances
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    maxItemsInEachRow = gridColumns,
  ) {
    items.forEach { item ->
      ItemChip(item = item, onItemClick = onItemClick)
    }
  }
}

@Composable
private fun ItemChip(
  item: ItemDetailUiModel,
  onItemClick: (ItemDetailUiModel) -> Unit,
  modifier: Modifier = Modifier,
) {
  SuggestionChip(
    modifier = modifier,
    onClick = { onItemClick(item) },
    contentPadding = PaddingValues(start = 0.dp, end = 8.dp),
    icon = if (item.packSize != null && item.packSize > 1) {
      {
        Text(
          text = item.packSize.toString(),
          modifier = Modifier
            .clip(CaducityTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.primaryContainer),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }
    } else {
      null
    },
    label = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(text = item.text)
      }
    },
  )
}

// Preview Data
private val yesterday = today.minusDays(1)
private val nextWeek = LocalDate(2024, 2, 22)

class ProductTabContentPreviewProvider :
  CollectionPreviewParameterProvider<ProductPageState>(
    listOf(
      // Loading state
      ProductPageState.Loading,
      // Empty state
      ProductPageState.Success(
        datedItemsGroups = persistentListOf(),
        frozenItems = persistentListOf(),
        consumedItems = persistentListOf(),
      ),
      // Product with all statuses
      ProductPageState.Success(
        datedItemsGroups = persistentListOf(
          DateItemsUiModel(
            text = "Yesterday",
            status = ItemStatus.Expired,
            date = yesterday,
            items = persistentListOf(
              ItemDetailUiModel(
                id = "1",
                expirationDate = yesterday,
                status = ItemStatus.Expired,
                text = "Expired package",
                packSize = Random.nextInt(4),
              ),
            ),
          ),
          DateItemsUiModel(
            text = "Today",
            status = ItemStatus.ExpiringSoon,
            date = today,
            items = persistentListOf(
              ItemDetailUiModel(
                id = "2",
                expirationDate = today,
                status = ItemStatus.ExpiringSoon,
                text = "Expiring package 1",
                packSize = Random.nextInt(4),
              ),
              ItemDetailUiModel(
                id = "3",
                expirationDate = today,
                status = ItemStatus.ExpiringSoon,
                text = "Expiring package 2",
                packSize = Random.nextInt(4),
              ),
            ),
          ),
          DateItemsUiModel(
            text = "In 7 days",
            status = ItemStatus.Fresh,
            date = nextWeek,
            items = persistentListOf(
              ItemDetailUiModel(
                id = "4",
                expirationDate = nextWeek,
                status = ItemStatus.Fresh,
                text = "Fresh package 1",
                packSize = Random.nextInt(4),
              ),
              ItemDetailUiModel(
                id = "5",
                expirationDate = nextWeek,
                status = ItemStatus.Fresh,
                text = "Fresh package 2",
                packSize = Random.nextInt(4),
              ),
              ItemDetailUiModel(
                id = "6",
                expirationDate = nextWeek,
                status = ItemStatus.Fresh,
                text = "Fresh package 3",
                packSize = Random.nextInt(4),
              ),
            ),
          ),
        ),
        frozenItems = persistentListOf(
          ItemDetailUiModel(
            id = "7",
            expirationDate = today,
            status = ItemStatus.Frozen,
            text = "Frozen package 1",
            packSize = Random.nextInt(4),
          ),
          ItemDetailUiModel(
            id = "8",
            expirationDate = today,
            status = ItemStatus.Frozen,
            text = "Frozen package 2",
            packSize = Random.nextInt(4),
          ),
        ),
        consumedItems = persistentListOf(
          ItemDetailUiModel(
            id = "9",
            expirationDate = today,
            status = ItemStatus.Consumed,
            text = "Consumed package 1",
            packSize = Random.nextInt(4),
          ),
          ItemDetailUiModel(
            id = "10",
            expirationDate = today,
            status = ItemStatus.Consumed,
            text = "Consumed package 2",
            packSize = Random.nextInt(4),
          ),
        ),
      ),
      // Error state
      ProductPageState.Error("Failed to load items"),
    )
  ) {
  override fun getDisplayName(index: Int): String {
    return when (val state = values.toList()[index]) {
      is ProductPageState.Loading -> "Loading"
      is ProductPageState.Success -> {
        if (state.datedItemsGroups.isEmpty() &&
          state.frozenItems.isEmpty() &&
          state.consumedItems.isEmpty()
        ) {
          "Empty"
        } else {
          "With items"
        }
      }

      is ProductPageState.Error -> "Error"
    }
  }
}

@PreviewLightDark
@Composable
fun ProductTabContentPreview(
  @PreviewParameter(provider = ProductTabContentPreviewProvider::class)
  state: ProductPageState,
) {
  PreviewTheme {
    Surface {
      ProductTabContentPage(
        state = state,
        onItemClick = {},
      )
    }
  }
}

@PreviewLightDark
@Composable
fun ItemChipPreview() {
  PreviewTheme {
    Surface {
      ItemChip(
        modifier = Modifier.padding(8.dp),
        item = ItemDetailUiModel(
          id = "ex",
          expirationDate = LocalDate.now(),
          status = ItemStatus.ExpiringSoon,
          text = "Maduixa",
          packSize = 4,
        ),
        onItemClick = {},
      )
    }
  }
}
