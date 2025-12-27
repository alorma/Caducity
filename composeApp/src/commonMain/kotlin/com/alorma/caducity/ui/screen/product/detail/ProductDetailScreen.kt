package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.ui.screen.dashboard.ExpirationColors
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import com.alorma.caducity.base.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductDetailScreen(
  productId: String,
  onBack: () -> Unit,
  viewModel: ProductDetailViewModel = koinViewModel { parametersOf(productId) }
) {
  val state = viewModel.state.collectAsStateWithLifecycle()

  when (val currentState = state.value) {
    is ProductDetailState.Loading -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        LoadingIndicator(
          color = CaducityTheme.colorScheme.secondary,
          polygons = listOf(
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Cookie6Sided,
          ),
        )
      }
    }

    is ProductDetailState.Success -> {
      ProductDetailContent(
        product = currentState.product,
        onBack = onBack,
      )
    }

    is ProductDetailState.Error -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = currentState.message,
          style = MaterialTheme.typography.bodyLarge,
          color = CaducityTheme.colorScheme.error,
        )
      }
    }
  }
}

@Composable
private fun ProductDetailContent(
  product: ProductDetailUiModel,
  onBack: () -> Unit,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = product.name) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = AppIcons.Back,
              contentDescription = "Back",
            )
          }
        },
      )
    },
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Product description section
      if (product.description.isNotEmpty()) {
        item {
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
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                color = CaducityTheme.colorScheme.onSurface,
              )
              Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = CaducityTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      // Instances section header
      item {
        Text(
          text = "Instances (${product.instances.size})",
          style = MaterialTheme.typography.titleLarge,
          color = CaducityTheme.colorScheme.onSurface,
        )
      }

      // Instance cards
      items(product.instances) { instance ->
        InstanceCard(instance = instance)
      }
    }
  }
}

@Composable
private fun InstanceCard(instance: ProductInstanceDetailUiModel) {
  val colors = ExpirationColors.getSectionColors(instance.status)

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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Instance identifier and status indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = instance.identifier,
          style = MaterialTheme.typography.titleMedium,
          color = CaducityTheme.colorScheme.onSurface,
        )

        Box(
          modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(colors.container)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Text(
            text = when (instance.status) {
              InstanceStatus.Expired -> "Expired"
              InstanceStatus.ExpiringSoon -> "Expiring Soon"
              InstanceStatus.Fresh -> "Fresh"
            },
            style = MaterialTheme.typography.labelMedium,
            color = colors.onContainer,
          )
        }
      }

      // Expiration date
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Expires: ",
          style = MaterialTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = instance.expirationDate.toString(),
          style = MaterialTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurface,
        )
      }

      // Action buttons (placeholders)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        PlaceholderActionButton(
          text = "Remove/Consume",
          modifier = Modifier.weight(1f),
        )
        PlaceholderActionButton(
          text = "Freeze",
          modifier = Modifier.weight(1f),
        )
        PlaceholderActionButton(
          text = "Edit",
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun PlaceholderActionButton(
  text: String,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = CaducityTheme.colorScheme.secondaryContainer,
    ),
    shape = MaterialTheme.shapes.medium,
    onClick = { /* Placeholder - no action */ },
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = CaducityTheme.colorScheme.onSecondaryContainer,
      )
    }
  }
}
