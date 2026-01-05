package com.alorma.caducity.ui.screen.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toHorizontalShape
import com.alorma.caducity.ui.components.shape.toVerticalShape
import com.alorma.caducity.ui.screen.dashboard.components.productListWithInstancesPreview
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate
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
      .clip(MaterialTheme.shapes.medium)
      .background(CaducityTheme.colorScheme.surfaceContainer)
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

          val instanceGroupSize = group.instances.size

          LazyRow(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(group.instances.toList()) { instance ->
              val index = group.instances.indexOf(instance)
              val shapePosition = when {
                instanceGroupSize == 1 -> ShapePosition.Single
                index == 0 -> ShapePosition.Start
                index == instanceGroupSize - 1 -> ShapePosition.End
                else -> ShapePosition.Middle
              }

              val itemModifier = if (instanceGroupSize > 1) {
                Modifier.fillParentMaxWidth(0.80f)
              } else {
                Modifier.fillParentMaxWidth()
              }

              ProductInstanceCard(
                modifier = itemModifier,
                instance = instance,
                today = today,
                relativeTimeFormatter = relativeTimeFormatter,
                shapePosition = shapePosition,
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

@Composable
private fun ProductInstanceCard(
  instance: ProductsListInstanceUiModel,
  today: LocalDate,
  relativeTimeFormatter: RelativeTimeFormatter,
  shapePosition: ShapePosition,
  modifier: Modifier = Modifier,
) {
  var relativeTimeText by remember { mutableStateOf("") }

  LaunchedEffect(instance.expirationDate, today) {
    relativeTimeText = relativeTimeFormatter.format(today, instance.expirationDate)
  }

  Column(
    modifier = modifier
      .width(120.dp)
      .clip(shapePosition.toHorizontalShape())
      .background(CaducityTheme.colorScheme.surfaceContainerHighest)
      .padding(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    StatusBadge(status = instance.status)

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = relativeTimeText,
        style = MaterialTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurface,
      )
      Text(
        text = instance.expirationDateText,
        style = MaterialTheme.typography.labelSmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
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