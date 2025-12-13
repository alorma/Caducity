package com.alorma.caducity.ui.screen.dashboard.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.screen.dashboard.ExpirationColors
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductInstanceUiModel
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import com.alorma.caducity.ui.theme.AppTheme
import com.alorma.caducity.ui.theme.CaducityTheme
import com.kizitonwose.calendar.core.now
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProductItem(
  product: ProductUiModel,
  isExpanded: Boolean,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = CaducityTheme.colorScheme.surfaceContainer,
    ),
    shape = MaterialTheme.shapes.largeIncreased,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {}
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = product.name,
        style = MaterialTheme.typography.titleMedium,
        color = CaducityTheme.colorScheme.onSurface,
      )

      if (isExpanded && product.description.isNotEmpty()) {
        Text(
          text = product.description,
          style = MaterialTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }

      when (product) {
        is ProductUiModel.WithInstances -> {
          if (isExpanded) {
            ExpandedInstancesView(product.instances)
          } else {
            CollapsedInstancesView(product.instances)
          }
        }

        is ProductUiModel.Empty -> {
          Text(
            text = "No active instances",
            style = MaterialTheme.typography.bodySmall,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun CollapsedInstancesView(instances: List<ProductInstanceUiModel>) {
  // Group instances by status and count them
  val statusCounts = instances.groupBy { it.status }
    .mapValues { it.value.size }
  
  // Sort by status priority: Expired, ExpiringSoon, Fresh
  val orderedStatuses = listOf(
    InstanceStatus.Expired,
    InstanceStatus.ExpiringSoon,
    InstanceStatus.Fresh
  ).filter { statusCounts.containsKey(it) }

  Row(
    modifier = Modifier
      .height(40.dp)
      .clip(MaterialTheme.shapes.small),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    orderedStatuses.forEach { status ->
      val count = statusCounts.getValue(status)
      val colors = ExpirationColors.getSectionColors(status)

      Box(
        modifier = Modifier
          .weight(1f)
          .background(colors.container)
          .padding(8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = count.toString(),
          style = MaterialTheme.typography.titleMedium,
          color = colors.onContainer,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
private fun ExpandedInstancesView(instances: List<ProductInstanceUiModel>) {
  Row(
    modifier = Modifier
      .height(20.dp)
      .clip(MaterialTheme.shapes.small),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    instances.forEach { instance ->
      val colors = ExpirationColors.getSectionColors(instance.status)

      Box(
        modifier = Modifier
          .weight(1f)
          .background(colors.container)
          .padding(20.dp),
      )
    }
  }
  
  // List individual instances
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    instances.forEach { instance ->
      val colors = ExpirationColors.getSectionColors(instance.status)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(MaterialTheme.shapes.extraSmall)
          .background(colors.container.copy(alpha = 0.3f))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = when (instance.status) {
            InstanceStatus.Expired -> "Expired"
            InstanceStatus.ExpiringSoon -> "Expiring Soon"
            InstanceStatus.Fresh -> "Fresh"
          },
          style = MaterialTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurface,
        )
        Text(
          text = instance.expirationDate.toString(),
          style = MaterialTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Preview
@Composable
private fun ProductItemPreview() {
  AppTheme {
    Surface {
      ProductItem(

        product = ProductUiModel.WithInstances(
          id = "condimentum",
          name = "Carolina Jordan",
          description = "faucibus",
          today = LocalDate.now(),
          instances = listOf(
            ProductInstanceUiModel(
              id = "atomorum",
              status = InstanceStatus.Expired,
              expirationDate = LocalDate.now(),
            ),
            ProductInstanceUiModel(
              id = "atomorum",
              status = InstanceStatus.Expired,
              expirationDate = LocalDate.now(),
            ),
            ProductInstanceUiModel(
              id = "atomorum",
              status = InstanceStatus.ExpiringSoon,
              expirationDate = LocalDate.now(),
            ),
            ProductInstanceUiModel(
              id = "atomorum",
              status = InstanceStatus.ExpiringSoon,
              expirationDate = LocalDate.now(),
            ),
            ProductInstanceUiModel(
              id = "atomorum",
              status = InstanceStatus.Fresh,
              expirationDate = LocalDate.now(),
            ),
            ProductInstanceUiModel(
              id = "atomorum",
              status = InstanceStatus.Fresh,
              expirationDate = LocalDate.now(),
            ),
          ),
        ),
        isExpanded = true,
      )
    }
  }
}