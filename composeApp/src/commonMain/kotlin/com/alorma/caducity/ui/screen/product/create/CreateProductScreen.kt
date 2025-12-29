package com.alorma.caducity.ui.screen.product.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.create_product_add_instance
import caducity.composeapp.generated.resources.create_product_button_cancel
import caducity.composeapp.generated.resources.create_product_button_create
import caducity.composeapp.generated.resources.create_product_content_description_back
import caducity.composeapp.generated.resources.create_product_description_label
import caducity.composeapp.generated.resources.create_product_description_placeholder
import caducity.composeapp.generated.resources.create_product_instance_number
import caducity.composeapp.generated.resources.create_product_instances_title
import caducity.composeapp.generated.resources.create_product_name_label
import caducity.composeapp.generated.resources.create_product_name_placeholder
import caducity.composeapp.generated.resources.create_product_remove_instance
import caducity.composeapp.generated.resources.create_product_screen_title
import com.alorma.caducity.barcode.BarcodeHandler
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.icons.BarcodeScanner
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateProductScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CreateProductViewModel = koinViewModel(),
  barcodeHandler: BarcodeHandler = koinInject(),
) {
  val state = viewModel.state.collectAsStateWithLifecycle()
  var showInstanceBottomSheet by remember { mutableStateOf(false) }
  var editingInstanceId by remember { mutableStateOf<String?>(null) }
  var scannedBarcode by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()

  // Register permission contract for barcode scanning
  barcodeHandler.registerPermissionContract()

  CreateProductPage(
    state = state.value,
    onNameChange = viewModel::updateName,
    onDescriptionChange = viewModel::updateDescription,
    onAddInstance = {
      editingInstanceId = null
      scannedBarcode = null
      showInstanceBottomSheet = true
    },
    onScanBarcode = {
      coroutineScope.launch {
        barcodeHandler.scan { barcode ->
          editingInstanceId = null
          scannedBarcode = barcode.data
          showInstanceBottomSheet = true
        }
      }
    },
    hasBarcodeCapability = barcodeHandler.hasBarcodeCapability(),
    onEditInstance = { instanceId ->
      editingInstanceId = instanceId
      scannedBarcode = null
      showInstanceBottomSheet = true
    },
    onRemoveInstance = viewModel::removeInstance,
    onCreateClick = { viewModel.createProduct(onBack) },
    onBackClick = onBack,
    onErrorDismiss = viewModel::clearError,
    modifier = modifier,
  )

  // Instance Bottom Sheet
  if (showInstanceBottomSheet) {
    val instance = state.value.instances.firstOrNull { it.id == editingInstanceId }

    CreateInstanceBottomSheet(
      instanceId = editingInstanceId,
      instance = instance,
      scannedBarcode = scannedBarcode,
      onSave = { identifier, expirationDate ->
        if (editingInstanceId != null) {
          viewModel.updateInstanceIdentifier(editingInstanceId!!, identifier)
          viewModel.updateInstanceExpirationDate(editingInstanceId!!, expirationDate)
        } else {
          viewModel.addInstance()
          val newInstance = viewModel.state.value.instances.last()
          viewModel.updateInstanceIdentifier(newInstance.id, identifier)
          viewModel.updateInstanceExpirationDate(newInstance.id, expirationDate)
        }
        scannedBarcode = null
        showInstanceBottomSheet = false
      },
      onDismiss = {
        scannedBarcode = null
        showInstanceBottomSheet = false
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProductPage(
  state: CreateProductState,
  onNameChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onAddInstance: () -> Unit,
  onScanBarcode: () -> Unit,
  hasBarcodeCapability: Boolean,
  onEditInstance: (String) -> Unit,
  onRemoveInstance: (String) -> Unit,
  onCreateClick: () -> Unit,
  onBackClick: () -> Unit,
  onErrorDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.systemBars,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(Res.string.create_product_screen_title),
            style = MaterialTheme.typography.headlineMedium,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = AppIcons.Back,
              contentDescription = stringResource(Res.string.create_product_content_description_back)
            )
          }
        }
      )
    },
    bottomBar = {
      BottomAppBar {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(
            onClick = onBackClick,
            enabled = !state.isLoading,
            modifier = Modifier.weight(1f),
          ) {
            Text(stringResource(Res.string.create_product_button_cancel))
          }
          Button(
            onClick = onCreateClick,
            enabled = !state.isLoading,
            modifier = Modifier.weight(1f),
          ) {
            if (state.isLoading) {
              CircularProgressIndicator()
            } else {
              Text(stringResource(Res.string.create_product_button_create))
            }
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 24.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Product Name Field
      TextField(
        value = state.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(Res.string.create_product_name_label)) },
        placeholder = { Text(stringResource(Res.string.create_product_name_placeholder)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading,
        isError = state.error != null,
      )

      // Product Description Field
      TextField(
        value = state.description,
        onValueChange = onDescriptionChange,
        label = { Text(stringResource(Res.string.create_product_description_label)) },
        placeholder = { Text(stringResource(Res.string.create_product_description_placeholder)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        enabled = !state.isLoading,
      )

      // Instances Section
      Text(
        text = stringResource(Res.string.create_product_instances_title),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 8.dp)
      )

      // Instance List
      state.instances.forEachIndexed { index, instance ->
        ProductInstanceCard(
          instance = instance,
          instanceNumber = index + 1,
          canRemove = state.instances.size > 1,
          isLoading = state.isLoading,
          onEdit = { onEditInstance(instance.id) },
          onRemove = { onRemoveInstance(instance.id) },
        )
      }

      // Add Instance Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedButton(
          onClick = onAddInstance,
          modifier = Modifier.weight(1f),
          enabled = !state.isLoading,
        ) {
          Text(stringResource(Res.string.create_product_add_instance))
        }

        if (hasBarcodeCapability) {
          OutlinedIconButton(
            onClick = onScanBarcode,
            enabled = !state.isLoading,
          ) {
            Icon(
              imageVector = AppIcons.BarcodeScanner,
              contentDescription = "Scan barcode",
            )
          }
        }
      }

      // Error Message
      if (state.error != null) {
        Text(
          text = state.error,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
        )
        LaunchedEffect(state.error, onErrorDismiss) {
          kotlinx.coroutines.delay(3000)
          onErrorDismiss()
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun ProductInstanceCard(
  instance: ProductInstanceInput,
  instanceNumber: Int,
  canRemove: Boolean,
  isLoading: Boolean,
  onEdit: () -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    onClick = onEdit,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(
            resource = Res.string.create_product_instance_number,
            instanceNumber,
          ),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
        )
        if (canRemove) {
          TextButton(
            onClick = onRemove,
            enabled = !isLoading,
          ) {
            Text(stringResource(Res.string.create_product_remove_instance))
          }
        }
      }

      if (instance.identifier.isNotBlank()) {
        Text(
          text = "ID: ${instance.identifier}",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      if (instance.expirationDateText != null) {
        Text(
          text = "Expires: ${instance.expirationDateText}",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      if (instance.identifier.isBlank() && instance.expirationDateText == null) {
        Text(
          text = "Tap to add details",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
