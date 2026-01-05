package com.alorma.caducity.ui.screen.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toHorizontalShape
import com.alorma.caducity.ui.screen.dashboard.components.productListWithInstancesPreview
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
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
              text = "${group.identifier} (${group.instances.size})",
              style = MaterialTheme.typography.labelLarge,
              color = CaducityTheme.colorScheme.onSurface,
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(24.dp)
              .clip(ShapePosition.Single.toHorizontalShape()),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            group.instances.forEach { instance ->
              val colors = ExpirationDefaults.getVibrantColors(instance.status)
              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .weight(1f)
                  .background(colors.container),
              )
            }
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

@Preview
@Composable
private fun ProductsListItemPreviewTheme() {
  PreviewTheme {
    ProductsListItem(
      product = productListWithInstancesPreview,
      onClick = {},
    )
  }
}