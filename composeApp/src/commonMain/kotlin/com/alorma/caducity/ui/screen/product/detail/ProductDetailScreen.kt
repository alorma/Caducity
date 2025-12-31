package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.base.ui.components.StatusBadge
import com.alorma.caducity.base.ui.components.StatusBadgeSize
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.create_product_add_instance
import com.alorma.caducity.base.ui.components.StyledTopAppBar
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.ui.screen.product.create.CreateInstanceBottomSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductDetailScreen(
  productId: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductDetailViewModel = koinViewModel { parametersOf(productId) }
) {
  val state = viewModel.state.collectAsStateWithLifecycle()
  var showInstanceBottomSheet by remember { mutableStateOf(false) }

  when (val currentState = state.value) {
    is ProductDetailState.Loading -> {
      Box(
        modifier = Modifier.fillMaxSize().then(modifier),
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
        onAddInstance = { showInstanceBottomSheet = true },
        onDeleteInstance = { instanceId -> viewModel.deleteInstance(instanceId) },
        onConsumeInstance = { instanceId -> viewModel.consumeInstance(instanceId) },
        onToggleFreezeInstance = { instanceId, expirationInstant, isFrozen ->
          viewModel.toggleFreezeInstance(instanceId, expirationInstant, isFrozen)
        },
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

  // Instance Bottom Sheet
  if (showInstanceBottomSheet) {
    CreateInstanceBottomSheet(
      instanceId = null,
      instance = null,
      onSave = { identifier, expirationDate ->
        viewModel.addInstance(identifier, expirationDate) {
          showInstanceBottomSheet = false
        }
      },
      onDismiss = {
        showInstanceBottomSheet = false
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailContent(
  product: ProductDetailUiModel,
  onBack: () -> Unit,
  onAddInstance: () -> Unit,
  onDeleteInstance: (String) -> Unit,
  onConsumeInstance: (String) -> Unit,
  onToggleFreezeInstance: (String, kotlin.time.Instant, Boolean) -> Unit,
) {
  Scaffold(
    topBar = {
      StyledTopAppBar(
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
                style = MaterialTheme.typography.titleLarge,
                color = CaducityTheme.colorScheme.onSurface,
              )
              Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge,
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
          style = MaterialTheme.typography.headlineSmall,
          color = CaducityTheme.colorScheme.onSurface,
        )
      }

      // Add Instance button
      item {
        OutlinedButton(
          onClick = onAddInstance,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(stringResource(Res.string.create_product_add_instance))
        }
      }

      // Instance cards
      items(product.instances) { instance ->
        InstanceCard(
          instance = instance,
          onDeleteInstance = onDeleteInstance,
          onConsumeInstance = onConsumeInstance,
          onToggleFreezeInstance = onToggleFreezeInstance,
        )
      }
    }
  }
}

@Composable
private fun InstanceCard(
  instance: ProductInstanceDetailUiModel,
  onDeleteInstance: (String) -> Unit,
  onConsumeInstance: (String) -> Unit,
  onToggleFreezeInstance: (String, kotlin.time.Instant, Boolean) -> Unit,
) {
  var showDeleteConfirmation by remember { mutableStateOf(false) }
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = CaducityTheme.colorScheme.surfaceContainer,
    ),
    shape = MaterialTheme.shapes.largeIncreased,
    elevation = CardDefaults.cardElevation(
      defaultElevation = 2.dp,
    ),
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
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
          ),
          color = CaducityTheme.colorScheme.onSurface,
        )

        StatusBadge(
          status = instance.status,
          size = StatusBadgeSize.Medium,
        )
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
          text = instance.expirationDateText,
          style = MaterialTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurface,
        )
      }

      // Action buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // Delete button
        ActionButton(
          text = "Delete",
          modifier = Modifier.weight(1f),
          onClick = { showDeleteConfirmation = true },
        )

        // Consume button
        ActionButton(
          text = "Consume",
          modifier = Modifier.weight(1f),
          onClick = { onConsumeInstance(instance.id) },
        )

        // Freeze/Unfreeze button
        val isFrozen = instance.status == InstanceStatus.Frozen
        ActionButton(
          text = if (isFrozen) "Unfreeze" else "Freeze",
          modifier = Modifier.weight(1f),
          onClick = {
            onToggleFreezeInstance(instance.id, instance.expirationInstant, isFrozen)
          },
        )
      }
    }
  }

  // Delete confirmation dialog
  if (showDeleteConfirmation) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmation = false },
      title = { Text("Delete Instance?") },
      text = { Text("Are you sure you want to delete \"${instance.identifier}\"? This action cannot be undone.") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteInstance(instance.id)
            showDeleteConfirmation = false
          }
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showDeleteConfirmation = false }
        ) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun ActionButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Expressive spring animation for press effect
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(
      dampingRatio = 0.6f,
      stiffness = 400f,
    ),
    label = "button_press_scale"
  )

  Card(
    modifier = modifier.graphicsLayer {
      scaleX = scale
      scaleY = scale
    },
    colors = CardDefaults.cardColors(
      containerColor = CaducityTheme.colorScheme.secondaryContainer,
    ),
    shape = MaterialTheme.shapes.medium,
    onClick = onClick,
    interactionSource = interactionSource,
    elevation = CardDefaults.cardElevation(
      defaultElevation = 1.dp,
      pressedElevation = 2.dp,
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Medium,
        ),
        color = CaducityTheme.colorScheme.onSecondaryContainer,
      )
    }
  }
}
