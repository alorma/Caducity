package com.alorma.caducity.ui.screen.category.detail.product

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.StatusBadgeSize
import com.alorma.caducity.ui.components.calendar.today
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailProductTabUiModel
import com.alorma.caducity.ui.screen.category.detail.DateItemsUiModel
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.alorma.caducity.ui.theme.preview.ScreenshotPreviewTheme
import com.kizitonwose.calendar.core.minusDays

@Composable
fun ProductTabContent(
  productTab: CategoryDetailProductTabUiModel,
  onItemClick: (ItemDetailUiModel) -> Unit,
) {
  when (productTab) {
    is CategoryDetailProductTabUiModel.Empty -> {
      // Show empty state for products with no items
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(R.string.category_detail_product_empty_state),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    is CategoryDetailProductTabUiModel.WithItems -> {
      val availableItemsCount = productTab.datedItemsGroups.sumOf { it.items.size }
      val frozenItemsCount = productTab.frozenItems.size
      val consumedItemsCount = productTab.consumedItems.size

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Sticky header for available items
        if (productTab.datedItemsGroups.isNotEmpty()) {
          stickyHeader {
            SectionHeader(
              title = stringResource(R.string.category_detail_section_available),
              count = availableItemsCount,
            )
          }
        }

        // Show each dated status group
        items(productTab.datedItemsGroups) { datedItems ->
          StatusGroupCard(
            datedItems = datedItems,
            onItemClick = onItemClick,
          )
        }

        // Sticky header for frozen items
        if (productTab.frozenItems.isNotEmpty()) {
          stickyHeader {
            SectionHeader(
              title = stringResource(R.string.category_detail_section_frozen),
              count = frozenItemsCount,
            )
          }

          item {
            FrozenItemsGroupCard(
              frozenItems = productTab.frozenItems,
              onItemClick = onItemClick,
            )
          }
        }

        // Sticky header for consumed items
        if (productTab.consumedItems.isNotEmpty()) {
          stickyHeader {
            SectionHeader(
              title = stringResource(R.string.category_detail_section_consumed),
              count = consumedItemsCount,
            )
          }

          item {
            ConsumedItemsGroupCard(
              consumedItems = productTab.consumedItems,
              onItemClick = onItemClick,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatusGroupCard(
  datedItems: DateItemsUiModel,
  onItemClick: (ItemDetailUiModel) -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Show expiration date and status
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      StatusBadge(
        status = datedItems.status,
        size = StatusBadgeSize.Large,
      )

      if (datedItems.text.isNotEmpty()) {
        Text(
          text = "·",
          style = CaducityTheme.typography.labelMedium,
        )

        Text(
          text = datedItems.text,
          style = CaducityTheme.typography.labelMedium,
        )
      }
    }

    // Show instances
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      maxItemsInEachRow = 3,
    ) {
      val statusColors = ExpirationDefaults.getSoftColors(datedItems.status)

      val chipColors = SuggestionChipDefaults.suggestionChipColors(
        containerColor = statusColors.container,
      )

      datedItems.items.forEach { item ->
        SuggestionChip(
          onClick = { onItemClick(item) },
          colors = chipColors,
          label = { Text(text = item.text) },
        )
      }
    }
  }
}

@Composable
private fun FrozenItemsGroupCard(
  frozenItems: List<ItemDetailUiModel>,
  onItemClick: (ItemDetailUiModel) -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Show frozen status badge
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      StatusBadge(
        status = ItemStatus.Frozen,
        size = StatusBadgeSize.Large,
      )

      Text(
        text = "·",
        style = CaducityTheme.typography.labelMedium,
      )

      Text(
        text = stringResource(R.string.category_detail_frozen_items),
        style = CaducityTheme.typography.labelMedium,
      )
    }

    // Show frozen instances
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      maxItemsInEachRow = 3,
    ) {
      val statusColors = ExpirationDefaults.getSoftColors(ItemStatus.Frozen)

      val chipColors = SuggestionChipDefaults.suggestionChipColors(
        containerColor = statusColors.container,
      )

      frozenItems.forEach { item ->
        SuggestionChip(
          onClick = { onItemClick(item) },
          colors = chipColors,
          label = { Text(text = item.text) },
        )
      }
    }
  }
}

@Composable
private fun ConsumedItemsGroupCard(
  consumedItems: List<ItemDetailUiModel>,
  onItemClick: (ItemDetailUiModel) -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Show consumed status badge
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      StatusBadge(
        status = ItemStatus.Consumed,
        size = StatusBadgeSize.Large,
      )

      Text(
        text = "·",
        style = CaducityTheme.typography.labelMedium,
      )

      Text(
        text = stringResource(R.string.category_detail_consumed_items),
        style = CaducityTheme.typography.labelMedium,
      )
    }

    // Show consumed instances
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      maxItemsInEachRow = 3,
    ) {
      val statusColors = ExpirationDefaults.getSoftColors(ItemStatus.Consumed)

      val chipColors = SuggestionChipDefaults.suggestionChipColors(
        containerColor = statusColors.container,
      )

      consumedItems.forEach { item ->
        SuggestionChip(
          onClick = { onItemClick(item) },
          colors = chipColors,
          label = { Text(text = item.text) },
        )
      }
    }
  }
}

@Composable
private fun SectionHeader(
  title: String,
  count: Int,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
    color = CaducityTheme.colorScheme.surface,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = title,
        style = CaducityTheme.typography.titleMedium,
        color = CaducityTheme.colorScheme.onSurface,
      )

      Surface(
        shape = MaterialTheme.shapes.small,
        color = CaducityTheme.colorScheme.secondaryContainer,
      ) {
        Text(
          text = count.toString(),
          style = CaducityTheme.typography.labelMedium,
          color = CaducityTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    }
  }
}

// Preview Data
private val yesterday = today.minusDays(1)
private val nextWeek = kotlinx.datetime.LocalDate(2024, 2, 22)

class ProductTabContentPreviewProvider :
  CollectionPreviewParameterProvider<CategoryDetailProductTabUiModel>(
    listOf(
      // Empty state
      CategoryDetailProductTabUiModel.Empty(
        id = "1",
        name = "Milk",
      ),
      // Product with all statuses
      CategoryDetailProductTabUiModel.WithItems(
        id = "3",
        name = "Cheese",
        datedItemsGroups = kotlinx.collections.immutable.persistentListOf(
          DateItemsUiModel(
            text = "Yesterday",
            status = ItemStatus.Expired,
            date = yesterday,
            items = kotlinx.collections.immutable.persistentListOf(
              ItemDetailUiModel(
                id = "1",
                expirationDate = yesterday,
                status = ItemStatus.Expired,
                text = "Expired package",
              ),
            ),
          ),
          DateItemsUiModel(
            text = "Today",
            status = ItemStatus.ExpiringSoon,
            date = today,
            items = kotlinx.collections.immutable.persistentListOf(
              ItemDetailUiModel(
                id = "2",
                expirationDate = today,
                status = ItemStatus.ExpiringSoon,
                text = "Expiring package 1",
              ),
              ItemDetailUiModel(
                id = "3",
                expirationDate = today,
                status = ItemStatus.ExpiringSoon,
                text = "Expiring package 2",
              ),
            ),
          ),
          DateItemsUiModel(
            text = "In 7 days",
            status = ItemStatus.Fresh,
            date = nextWeek,
            items = kotlinx.collections.immutable.persistentListOf(
              ItemDetailUiModel(
                id = "4",
                expirationDate = nextWeek,
                status = ItemStatus.Fresh,
                text = "Fresh package 1",
              ),
              ItemDetailUiModel(
                id = "5",
                expirationDate = nextWeek,
                status = ItemStatus.Fresh,
                text = "Fresh package 2",
              ),
              ItemDetailUiModel(
                id = "6",
                expirationDate = nextWeek,
                status = ItemStatus.Fresh,
                text = "Fresh package 3",
              ),
            ),
          ),
        ),
        frozenItems = kotlinx.collections.immutable.persistentListOf(
          ItemDetailUiModel(
            id = "7",
            expirationDate = today,
            status = ItemStatus.Frozen,
            text = "Frozen package 1",
          ),
          ItemDetailUiModel(
            id = "8",
            expirationDate = today,
            status = ItemStatus.Frozen,
            text = "Frozen package 2",
          ),
        ),
        consumedItems = kotlinx.collections.immutable.persistentListOf(
          ItemDetailUiModel(
            id = "9",
            expirationDate = today,
            status = ItemStatus.Consumed,
            text = "Consumed package 1",
          ),
          ItemDetailUiModel(
            id = "10",
            expirationDate = today,
            status = ItemStatus.Consumed,
            text = "Consumed package 2",
          ),
        ),
      ),
    )
  ) {
  override fun getDisplayName(index: Int): String {
    return when (values.toList()[index]) {
      is CategoryDetailProductTabUiModel.Empty -> "Empty"
      is CategoryDetailProductTabUiModel.WithItems -> "With status"
    }
  }
}

@PreviewLightDark
@Composable
fun ProductTabContentPreview(
  @PreviewParameter(provider = ProductTabContentPreviewProvider::class)
  productTab: CategoryDetailProductTabUiModel,
) {
  PreviewTheme {
    Surface {
      ProductTabContent(
        productTab = productTab,
        onItemClick = {},
      )
    }
  }
}
