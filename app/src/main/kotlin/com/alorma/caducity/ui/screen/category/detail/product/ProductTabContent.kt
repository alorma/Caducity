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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.calendar.today
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailProductTabUiModel
import com.alorma.caducity.ui.screen.category.detail.DateItemsUiModel
import com.alorma.caducity.ui.screen.category.detail.ItemDetailUiModel
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.kizitonwose.calendar.core.minusDays
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

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
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
          top = 16.dp,
          start = 16.dp,
          end = 16.dp,
          bottom = 80.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {

        productTab.datedItemsGroups.forEach { datedItems ->
          item {
            SectionHeader(
              status = datedItems.status,
              date = datedItems.text,
              title = ExpirationDefaults.getTitle(datedItems.status),
              count = datedItems.items.size,
            )
          }
          // Show each dated status group
          item {
            StatusGroupCard(
              items = datedItems.items,
              onItemClick = onItemClick,
            )
          }
        }

        // Sticky header for frozen items
        if (productTab.frozenItems.isNotEmpty()) {
          item {
            SectionHeader(
              status = ItemStatus.Frozen,
              title = stringResource(R.string.category_detail_section_frozen),
              count = productTab.frozenItems.size,
            )
          }

          item {
            StatusGroupCard(
              items = productTab.frozenItems,
              onItemClick = onItemClick,
            )
          }
        }

        // Sticky header for consumed items
        if (productTab.consumedItems.isNotEmpty()) {
          item {
            SectionHeader(
              status = ItemStatus.Consumed,
              title = stringResource(R.string.category_detail_section_consumed),
              count = productTab.consumedItems.size,
            )
          }

          item {
            StatusGroupCard(
              items = productTab.consumedItems,
              onItemClick = onItemClick,
            )
          }
        }
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
  val colors = ExpirationDefaults.getSoftColors(status)
  val vibrantColors = ExpirationDefaults.getVibrantColors(status)

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    shape = CaducityTheme.shapes.small,
    color = colors.container,
    contentColor = colors.onContainer,
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
          color = colors.onContainer,
        )

        Text(
          modifier = Modifier
            .clip(CaducityTheme.shapes.extraSmall)
            .background(vibrantColors.container)
            .padding(horizontal = 8.dp, vertical = 4.dp),
          text = count.toString(),
          color = vibrantColors.onContainer,
          style = CaducityTheme.typography.labelMedium,
        )
      }

      if (date != null) {
        Text(
          text = date,
          style = CaducityTheme.typography.labelSmallEmphasized,
          color = colors.onContainer,
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
  // Show instances
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    maxItemsInEachRow = 3,
  ) {
    items.forEach { item ->
      SuggestionChip(
        onClick = { onItemClick(item) },
        label = { Text(text = item.text) },
      )
    }
  }
}

// Preview Data
private val yesterday = today.minusDays(1)
private val nextWeek = LocalDate(2024, 2, 22)

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
            items = persistentListOf(
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
        frozenItems = persistentListOf(
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
        consumedItems = persistentListOf(
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
