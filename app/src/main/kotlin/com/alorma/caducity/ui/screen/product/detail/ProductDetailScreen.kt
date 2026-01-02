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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.InstanceActionError
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.StatusBadgeSize
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.screen.dashboard.CalendarMode
import com.alorma.caducity.ui.screen.dashboard.components.ProductsCalendar
import com.alorma.caducity.ui.screen.product.create.CreateInstanceBottomSheet
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
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
  val snackbarHostState = remember { SnackbarHostState() }

  // Error message strings
  val errorInstanceNotFound = stringResource(R.string.error_instance_not_found)
  val errorCannotFreezeExpired = stringResource(R.string.error_cannot_freeze_expired)
  val errorCannotConsumeExpired = stringResource(R.string.error_cannot_consume_expired)

  // Collect action errors and show them in snackbar
  LaunchedEffect(viewModel) {
    viewModel.actionError.collectLatest { error ->
      val message = when (error) {
        InstanceActionError.InstanceNotFound -> errorInstanceNotFound
        InstanceActionError.CannotFreezeExpiredInstance -> errorCannotFreezeExpired
        InstanceActionError.CannotConsumeExpiredInstance -> errorCannotConsumeExpired
      }
      snackbarHostState.showSnackbar(message)
    }
  }

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
        snackbarHostState = snackbarHostState,
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
      onSave = { identifier, expirationDate, quantity ->
        // For product detail screen, create instances one by one
        repeat(quantity) {
          viewModel.addInstance(identifier, expirationDate) {
            if (it == quantity - 1) {
              showInstanceBottomSheet = false
            }
          }
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
  snackbarHostState: SnackbarHostState,
  onBack: () -> Unit,
  onAddInstance: () -> Unit,
  onDeleteInstance: (String) -> Unit,
  onConsumeInstance: (String) -> Unit,
  onToggleFreezeInstance: (String, kotlin.time.Instant, Boolean) -> Unit,
) {
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  // Map instance IDs to their positions in the LazyColumn
  // Position 0: Description (if present)
  // Position 1: Calendar (if instances present)
  // Position 2: Instances header
  // Position 3: Add Instance button
  // Position 4+: Instance cards
  val instanceIndexMap = remember(product.instances) {
    val hasDescription = product.description.isNotEmpty()
    val hasCalendar = product.instances.isNotEmpty()

    val baseIndex = listOf(
      hasDescription, // 0 or not present
      hasCalendar,    // 1 or not present
      true,           // 2: header
      true,           // 3: add button
    ).count { it }

    product.instances.mapIndexed { index, instance ->
      instance.id to (baseIndex + index)
    }.toMap()
  }

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
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState) { data ->
        Snackbar(
          snackbarData = data,
          containerColor = CaducityTheme.colorScheme.errorContainer,
          contentColor = CaducityTheme.colorScheme.onErrorContainer,
        )
      }
    },
  ) { paddingValues ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
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

      // Calendar section
      if (product.instances.isNotEmpty()) {
        item {
          val calendarData = remember(product.instances) {
            product.instances.toCalendarData(hideConsumed = true)
          }

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
                .padding(vertical = 16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Expiration Timeline",
                style = MaterialTheme.typography.titleLarge,
                color = CaducityTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp),
              )
              ProductsCalendar(
                calendarData = calendarData,
                calendarMode = CalendarMode.WEEK,
                onDateClick = { date ->
                  // Find the first instance with this expiration date
                  val instance = product.instances.firstOrNull { it.expirationDate == date }
                  if (instance != null) {
                    val index = instanceIndexMap[instance.id]
                    if (index != null) {
                      coroutineScope.launch {
                        listState.animateScrollToItem(index)
                      }
                    }
                  }
                },
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
          Text(stringResource(R.string.create_product_add_instance))
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
