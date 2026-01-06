package com.alorma.caducity.ui.screen.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
  product: ProductListUiModel,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  appClock: AppClock = koinInject(),
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

    when (product) {
      is ProductListUiModel.WithContent -> {
        Spacer(modifier = Modifier.height(12.dp))

        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          product.variants.forEach { variant ->

            val totalCount = remember(variant.id) {
              variant.statusGroups.sumOf { it.count } + variant.frozenCount
            }

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
                text = "${variant.name} ($totalCount)",
                style = MaterialTheme.typography.labelLarge,
                color = CaducityTheme.colorScheme.onSurface,
              )
            }

            StatusBarsRow(statusGroups = variant.statusGroups)

            // Show frozen count as text
            if (variant.frozenCount > 0) {
              Text(
                text = stringResource(R.string.products_list_items_frozen, variant.frozenCount),
                style = MaterialTheme.typography.bodySmall,
                color = CaducityTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }
      }

      is ProductListUiModel.Empty -> {
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