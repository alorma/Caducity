package com.alorma.caducity.ui.screen.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.ui.screen.products.components.StatusBarsRow
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun ProductsListItem(
  product: ProductsListUiModel,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  appClock: AppClock = koinInject(),
  relativeTimeFormatter: RelativeTimeFormatter = koinInject(),
) {
  val today = remember(appClock) {
    appClock.now()
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date
  }
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(16.dp)
      .then(modifier),
  ) {
    Text(
      text = product.name,
      style = MaterialTheme.typography.titleMedium,
      color = CaducityTheme.colorScheme.onSurface,
    )

    if (product.description.isNotBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = product.description,
        style = MaterialTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }

    when (product) {
      is ProductsListUiModel.WithInstances -> {
        Spacer(modifier = Modifier.height(12.dp))
        product.groups.forEachIndexed { groupIndex, group ->
          // Add spacing between groups (except before the first one)
          if (groupIndex > 0) {
            Spacer(modifier = Modifier.height(12.dp))
          }

          // Group header with identifier and count
          val totalCount = group.statusGroups.sumOf { it.count } + group.frozenCount
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
          ) {
            Text(
              text = "—",
              style = MaterialTheme.typography.labelLarge,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(end = 8.dp)
            )
            Text(
              text = "${group.identifier} ($totalCount)",
              style = MaterialTheme.typography.labelLarge,
              color = CaducityTheme.colorScheme.onSurface,
            )
          }

          // Status color bars (excluding frozen)
          StatusBarsRow(statusGroups = group.statusGroups)

          // Show frozen count as text
          if (group.frozenCount > 0) {
            Text(
              text = stringResource(R.string.products_list_items_frozen, group.frozenCount),
              style = MaterialTheme.typography.bodySmall,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }

      is ProductsListUiModel.Empty -> {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "No instances",
          style = MaterialTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}