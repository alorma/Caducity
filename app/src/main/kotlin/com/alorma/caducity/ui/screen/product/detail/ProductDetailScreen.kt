package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHostState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarHostState
import com.alorma.caducity.ui.components.loading.WavyLoadingIndicator
import com.alorma.caducity.ui.components.scaffold.AppScaffold
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
        onBack = onBack,
        onNavigateToAddInstance = onNavigateToAddInstance,
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
  onBack: () -> Unit,
  onNavigateToAddInstance: () -> Unit,
) {
  AppScaffold(
    topBar = {
      StyledTopAppBar(
        title = { Text(text = state.product.name) },
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
    Column(
      modifier = Modifier.padding(paddingValues),
    ) {
      Text(
        text = "Variants",
        style = CaducityTheme.typography.headlineSmall,
      )
      state.variants.forEach { variant ->
        Text(text = variant.name)
        Column {
          variant.instances.forEachIndexed { index, instance ->
            if (instance.identifier.isEmpty()) {
              Text(text = "\tInstance $index")
            } else {
              Text(text = "\t${instance.identifier}")
            }
          }
        }
      }

      Text(
        text = "Standalone instances",
        style = CaducityTheme.typography.headlineSmall,
      )
      state.standaloneInstances.forEach { instance ->
        if (instance.identifier.isEmpty()) {
          Text(text = "No identifier")
        } else {
          Text(text = instance.identifier)
        }
      }
    }
  }
}
