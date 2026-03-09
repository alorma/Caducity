package com.alorma.caducity.ui.screen.filtered

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.feature.tracking.ProductItemsBottomSheetScreen
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun AppBottomSheetState.showProductItemsBottomSheet(
  coroutineScope: CoroutineScope,
  productName: String,
  items: List<Item>,
  onItemClick: (Item) -> Unit,
) {
  coroutineScope.launch {
    show(appFeedbackType = AppFeedbackType.Info) {
      ProductItemsBottomSheetContent(
        productName = productName,
        items = items,
        onItemClick = { item ->
          coroutineScope.launch {
            hide()
            onItemClick(item)
          }
        },
      )
    }
  }
}

@Composable
private fun ProductItemsBottomSheetContent(
  productName: String,
  items: List<Item>,
  onItemClick: (Item) -> Unit,
) {
  TrackScreen(screen = ProductItemsBottomSheetScreen())
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
  ) {
    // Header with product name and count
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        modifier = Modifier.weight(1f),
        text = productName,
        style = MaterialTheme.typography.titleMedium,
      )

      Text(
        text = stringResource(R.string.filtered_items_count, items.size),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    HorizontalDivider()

    // Items list
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
      items.forEach { item ->
        ItemRow(
          item = item,
          onClick = { onItemClick(item) },
        )
      }
    }
  }
}

@Composable
private fun ItemRow(
  item: Item,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(CaducityTheme.shapes.small)
        .clickable { onClick() }
        .padding(horizontal = 24.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      // Item identifier with pack badge
      Row(
        modifier =
          Modifier
            .clip(CaducityTheme.shapes.small),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Show pack badge if packSize > 1
        if (item.packSize != null && item.packSize > 1) {
          Text(
            text = item.packSize.toString(),
            modifier =
              Modifier
                .background(CaducityTheme.colorScheme.outline)
                .padding(6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.surface,
          )
        }

        Text(
          text = item.identifier,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(start = if (item.packSize != null && item.packSize > 1) 8.dp else 0.dp),
        )
      }

      Text(
        text = formatExpirationDate(item.expirationDate),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    StatusBadge(status = item.status)
  }
}

@Composable
private fun formatExpirationDate(instant: Instant): String {
  val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
  return localDate.toString()
}
