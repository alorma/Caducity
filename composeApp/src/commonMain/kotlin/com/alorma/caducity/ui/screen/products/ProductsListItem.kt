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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.dashboard.ExpirationDefaults
import com.alorma.caducity.ui.screen.dashboard.components.productListWithInstancesPreview

@Composable
fun ProductsListItem(
  product: ProductsListUiModel,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
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
        product.instances.forEach { instance ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = instance.identifier,
                style = MaterialTheme.typography.bodyMedium,
                color = CaducityTheme.colorScheme.onSurface,
              )
              Spacer(modifier = Modifier.width(8.dp))

              val colors = ExpirationDefaults.getColors(instance.status)

              Row(
                modifier = Modifier
                  .clip(MaterialTheme.shapes.small)
                  .background(colors.container.copy(CaducityTheme.dims.dim3))
                  .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Spacer(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.container)
                )
                Spacer(modifier = Modifier.width(4.dp))

                Text(
                  text = ExpirationDefaults.getText(instance.status),
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.onContainer,
                )
              }
            }

            Text(
              text = instance.expirationDateText,
              style = MaterialTheme.typography.bodySmall,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
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

@Preview
@Composable
private fun ProductsListItemPreview() {
  AppPreview {
    ProductsListItem(
      product = productListWithInstancesPreview,
      onClick = {},
    )
  }
}