package com.alorma.caducity.ui.screen.settings.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: DebugSettingsViewModel = koinViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  var showGenerateDialog by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
      when (effect) {
        is DebugSettingsSideEffect.GenerationSuccess -> {
          // Success handled by UI state
        }

        is DebugSettingsSideEffect.GenerationError -> {
          // Error handled by UI state
        }
      }
    }
  }
  AppScaffold(
    modifier = Modifier.then(modifier),
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = {
          Text(
            text = stringResource(R.string.settings_debug_title),
          )
        },
      )
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      // Notifications Group
      StyledSettingsGroup {
        StyledSettingsCard(
          title = "Test Notification",
          subtitle = "Trigger notification check immediately",
          position = ShapePosition.Single,
          onClick = { viewModel.onTriggerNotificationCheck() },
        )
      }

      // Fake Data Generation Group
      StyledSettingsGroup {
        StyledSettingsCard(
          title = "Generate Fake Data",
          subtitle = if (uiState.isGenerating) {
            uiState.progress?.toDisplayText() ?: "Generating..."
          } else {
            "Generate realistic grocery products with AI"
          },
          position = ShapePosition.Single,
          onClick = { if (!uiState.isGenerating) showGenerateDialog = true },
          action = if (uiState.isGenerating) {
            {
              CircularWavyProgressIndicator(
                modifier = Modifier.size(24.dp)
              )
            }
          } else null
        )
      }

      // Last generation result
      (uiState.progress as? GenerationProgress.Completed)?.let { result ->
        Text(
          text = "Last: ${result.productsCreated} products, ${result.variantsCreated} variants, ${result.instancesCreated} instances",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }

  // Configuration dialog
  if (showGenerateDialog) {
    GenerateFakeDataDialog(
      defaultMaxProducts = uiState.defaultMaxProducts,
      defaultMaxVariants = uiState.defaultMaxVariantsPerProduct,
      defaultMinInstances = uiState.defaultMinInstancesPerVariant,
      defaultMaxInstances = uiState.defaultMaxInstancesPerVariant,
      onDismiss = { showGenerateDialog = false },
      onConfirm = { products, variants, minInstances, maxInstances ->
        viewModel.onGenerateFakeData(products, variants, minInstances, maxInstances)
        showGenerateDialog = false
      }
    )
  }

  // Error dialog
  uiState.error?.let { error ->
    AlertDialog(
      onDismissRequest = { viewModel.dismissError() },
      title = { Text("Generation Failed") },
      text = { Text(error) },
      confirmButton = {
        TextButton(onClick = { viewModel.dismissError() }) {
          Text("OK")
        }
      }
    )
  }
}


@Composable
private fun GenerateFakeDataDialog(
  defaultMaxProducts: Int,
  defaultMaxVariants: Int,
  defaultMinInstances: Int,
  defaultMaxInstances: Int,
  onDismiss: () -> Unit,
  onConfirm: (products: Int, variants: Int, minInstances: Int, maxInstances: Int) -> Unit,
) {
  var maxProducts by remember { mutableIntStateOf(defaultMaxProducts) }
  var maxVariants by remember { mutableIntStateOf(defaultMaxVariants) }
  var minInstances by remember { mutableIntStateOf(defaultMinInstances) }
  var maxInstances by remember { mutableIntStateOf(defaultMaxInstances) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Generate Fake Data") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Configure generation parameters",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
          value = maxProducts.toString(),
          onValueChange = { maxProducts = it.toIntOrNull() ?: defaultMaxProducts },
          label = { Text("Max Products (1-10)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true
        )
        OutlinedTextField(
          value = maxVariants.toString(),
          onValueChange = { maxVariants = it.toIntOrNull() ?: defaultMaxVariants },
          label = { Text("Variants per Product (0-5)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true
        )
        OutlinedTextField(
          value = minInstances.toString(),
          onValueChange = { minInstances = it.toIntOrNull() ?: defaultMinInstances },
          label = { Text("Min Instances (1-20)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true
        )
        OutlinedTextField(
          value = maxInstances.toString(),
          onValueChange = { maxInstances = it.toIntOrNull() ?: defaultMaxInstances },
          label = { Text("Max Instances (1-20)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm(maxProducts, maxVariants, minInstances, maxInstances) },
        enabled = maxProducts in 1..10 &&
            maxVariants in 0..5 &&
            minInstances in 1..20 &&
            maxInstances in 1..20 &&
            minInstances <= maxInstances
      ) {
        Text("Generate")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private fun GenerationProgress.toDisplayText(): String = when (this) {
  is GenerationProgress.Started -> "Starting..."
  is GenerationProgress.CheckingExisting -> "Checking existing products..."
  is GenerationProgress.GeneratingWithAI -> "Generating with AI..."
  is GenerationProgress.MatchingProducts -> "Matching products..."
  is GenerationProgress.AwaitingReview -> "Awaiting review..."
  is GenerationProgress.InsertingToDatabase -> "Inserting $current/$total..."
  is GenerationProgress.Completed -> "Completed: $productsCreated products"
  is GenerationProgress.Failed -> "Failed"
}

@PreviewDynamicLightDark
@Composable
fun DebugSettingsScreenPreview() {
  PreviewTheme {
    Surface {
      DebugSettingsScreen()
    }
  }
}