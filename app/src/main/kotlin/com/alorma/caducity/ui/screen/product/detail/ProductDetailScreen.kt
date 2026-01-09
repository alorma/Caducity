package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Check
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.ThermometerSnow
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHostState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarHostState
import com.alorma.caducity.ui.components.loading.WavyLoadingIndicator
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.calculateShape
import com.alorma.caducity.ui.components.shape.toVerticalShape
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductDetailScreen(
  productId: String,
  onBack: () -> Unit,
  onNavigateToAddInstance: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductDetailViewModel = koinViewModel { parametersOf(productId) }
) {
  val state = viewModel.state.collectAsStateWithLifecycle()

  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarHostState()

  when (val currentState = state.value) {
    is ProductDetailState.Loading -> {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .then(modifier),
        contentAlignment = Alignment.Center,
      ) {
        WavyLoadingIndicator()
      }
    }

    is ProductDetailState.Success -> {
      ProductDetailContent(
        state = currentState,
        snackbarHostState = snackbarState,
        dialogState = dialogState,
        onNavigateToAddInstance = onNavigateToAddInstance,
        onConsumeInstance = viewModel::onConsumeInstance,
        onFreezeInstance = viewModel::onFreezeInstance,
        onDeleteInstance = viewModel::onDeleteInstance,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailContent(
  state: ProductDetailState.Success,
  snackbarHostState: AppSnackbarHostState,
  dialogState: AppDialogState,
  onNavigateToAddInstance: () -> Unit,
  onConsumeInstance: (String) -> Unit,
  onFreezeInstance: (String) -> Unit,
  onDeleteInstance: (String) -> Unit,
) {
  AppScaffold(
    topBar = {
      StyledTopAppBar(
        title = { Text(text = state.product.name) },
        navigationIcon = { NavigationIcon() },
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onNavigateToAddInstance) {
        Icon(
          imageVector = AppIcons.Add,
          contentDescription = "Add Instance",
        )
      }
    },
    snackbarState = snackbarHostState,
    dialogState = dialogState,
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
      contentPadding = PaddingValues(
        bottom = 90.dp,
      ),
    ) {
      // Variants section
      if (state.variants.isNotEmpty()) {
        item(key = "variants-title", contentType = "section-title") {
          SectionTitle(text = "Variants", isFirst = true)
        }

        state.variants.forEach { variant ->
          item(
            key = "variant-${variant.id}-title",
            contentType = "variant-title"
          ) {
            VariantTitle(name = variant.name)
          }

          itemsIndexed(
            items = variant.instances,
            key = { _, instance -> "variant-${variant.id}-instance-${instance.id}" },
            contentType = { _, _ -> "variant-instance" },
          ) { index, instance ->
            ProductInstanceCard(
              instance = instance,
              shapePosition = variant.instances.calculateShape(index),
              onConsume = { onConsumeInstance(instance.id) },
              onFreeze = { onFreezeInstance(instance.id) },
              onDelete = { onDeleteInstance(instance.id) },
            )
          }
        }
      }

      // Standalone instances section
      if (state.standaloneInstances.isNotEmpty()) {
        item(key = "standalone-title", contentType = "section-title") {
          SectionTitle(
            text = "Standalone instances",
            isFirst = state.variants.isEmpty(),
          )
        }

        itemsIndexed(
          items = state.standaloneInstances,
          key = { _, instance -> "standalone-instance-${instance.id}" },
          contentType = { _, _ -> "standalone-instance" },
        ) { index, instance ->
          ProductInstanceCard(
            instance = instance,
            shapePosition = state.standaloneInstances.calculateShape(index),
            onConsume = { onConsumeInstance(instance.id) },
            onFreeze = { onFreezeInstance(instance.id) },
            onDelete = { onDeleteInstance(instance.id) },
          )
        }
      }
    }
  }
}

@Composable
private fun SectionTitle(
  text: String,
  isFirst: Boolean,
  modifier: Modifier = Modifier,
) {
  Text(
    modifier = modifier.padding(
      top = if (isFirst) 0.dp else 24.dp,
      bottom = 8.dp
    ),
    text = text,
    style = MaterialTheme.typography.titleMedium,
    color = CaducityTheme.colorScheme.onSurface,
  )
}

@Composable
private fun VariantTitle(
  name: String,
  modifier: Modifier = Modifier,
) {
  Text(
    modifier = modifier.padding(top = 16.dp, bottom = 8.dp),
    text = name,
    style = MaterialTheme.typography.titleSmall,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun ProductInstanceCard(
  instance: ProductInstanceDetailUiModel,
  shapePosition: ShapePosition,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = shapePosition.toVerticalShape(),
    color = CaducityTheme.colorScheme.surfaceContainer,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        if (instance.identifier.isNotEmpty()) {
          Text(
            text = instance.identifier,
            style = MaterialTheme.typography.labelLarge,
            color = CaducityTheme.colorScheme.onSurface,
          )
        }
        Text(
          text = instance.expirationDateText,
          style = MaterialTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
      StatusBadge(instance.status)

      // Consume action button
      IconButton(
        onClick = onConsume
      ) {
        Icon(
          imageVector = AppIcons.Cooking,
          contentDescription = "Consume",
          tint = CaducityTheme.colorScheme.primary,
        )
      }

      // Freeze action button
      IconButton(
        onClick = onFreeze
      ) {
        Icon(
          imageVector = AppIcons.ThermometerSnow,
          contentDescription = "Freeze",
          tint = CaducityTheme.colorScheme.primary,
        )
      }

      // More actions dropdown (only Delete)
      Box {
        IconButton(onClick = { showMenu = true }) {
          Text(
            text = "⋮",
            style = MaterialTheme.typography.titleLarge,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
          )
        }
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = {
              Icon(
                imageVector = AppIcons.Delete,
                contentDescription = null,
              )
            },
            onClick = {
              onDelete()
              showMenu = false
            }
          )
        }
      }
    }
  }
}
