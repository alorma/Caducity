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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.screen.dashboard.ExpirationColors
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductInstanceUiModel
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import com.alorma.caducity.ui.theme.AppTheme
import com.kizitonwose.calendar.core.now
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProductItem(
  product: ProductUiModel,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
        color = MaterialTheme.colorScheme.onSurface,
      )

      if (product.description.isNotEmpty()) {
        Text(
          text = product.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      when (product) {
        is ProductUiModel.WithInstances -> {
          val instances = product.instances

          val statuses = instances.map { instance ->
            instance.status
          }

          Row(
            modifier = Modifier.height(20.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            statuses.forEachIndexed { index, status ->
              val colors = ExpirationColors.getSectionColors(status)

              val shape = if (statuses.size == 1) {
                MaterialTheme.shapes.small
              } else if (index == 0) {
                RoundedCornerShape(
                  topStart = MaterialTheme.shapes.small.topStart,
                  topEnd = CornerSize(0.dp),
                  bottomEnd = CornerSize(0.dp),
                  bottomStart = MaterialTheme.shapes.small.bottomStart,
                )
              } else if (index > 0 && index < (statuses.size - 1)) {
                RectangleShape
              } else {
                RoundedCornerShape(
                  topStart = CornerSize(0.dp),
                  topEnd = MaterialTheme.shapes.small.topStart,
                  bottomEnd = MaterialTheme.shapes.small.bottomStart,
                  bottomStart = CornerSize(0.dp),
                )
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(shape)
                  .background(colors.onContainer)
                  .padding(20.dp),
              )
            }
          }
        }

        is ProductUiModel.Empty -> {
          Text(
            text = "No active instances",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun ProductItemPreview() {
  AppTheme {
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
    )
  }
}