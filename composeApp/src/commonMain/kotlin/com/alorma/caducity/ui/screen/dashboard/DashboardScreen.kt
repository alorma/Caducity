package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.ui.adaptive.isWidthCompact
import com.alorma.caducity.ui.icons.AppIcons
import com.alorma.caducity.ui.icons.ArrowDown
import com.alorma.caducity.ui.icons.ArrowUp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel = koinViewModel()
) {
  val showDialog = remember { mutableStateOf(false) }

  val dashboardState by viewModel.state.collectAsStateWithLifecycle()

  val isCompact = isWidthCompact()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Dashboard",
      style = MaterialTheme.typography.headlineMedium,
    )

    when (val state = dashboardState) {
      is DashboardState.Loading -> {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          LoadingIndicator(
            color = MaterialTheme.colorScheme.secondary,
            polygons = listOf(
              MaterialShapes.Cookie4Sided,
              MaterialShapes.Cookie6Sided,
            ),
          )
        }
      }

      is DashboardState.Success -> {
        state.sections.forEach { section ->
          DashboardCard(
            title = stringResource(section.title),
            value = section.itemCount.toString(),
            products = section.products,
          )
        }
      }
    }
  }
}

@Composable
private fun DashboardCard(
  title: String,
  value: String,
  products: List<ProductInstance>,
) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded }
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Icon(
          imageVector = if (isExpanded) AppIcons.ArrowUp else AppIcons.ArrowDown,
          contentDescription = if (isExpanded) "Collapse" else "Expand",
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }

      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
      ) {
        Column {
          HorizontalDivider()

          if (products.isEmpty()) {
            Text(
              text = "No products in this category",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(16.dp),
            )
          } else {
            products.forEach { product ->
              ProductItem(product = product)
              if (product != products.last()) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProductItem(
  product: ProductInstance,
) {
  val expirationDate = product.expirationDate.toLocalDateTime(TimeZone.currentSystemDefault())
  val purchaseDate = product.purchaseDate.toLocalDateTime(TimeZone.currentSystemDefault())

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = "Product ID: ${product.productId}",
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
      text = "Instance ID: ${product.id}",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "Purchased: ${purchaseDate.date}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "Expires: ${expirationDate.date}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
