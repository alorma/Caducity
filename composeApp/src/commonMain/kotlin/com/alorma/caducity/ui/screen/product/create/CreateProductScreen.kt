package com.alorma.caducity.ui.screen.product.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateProductScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CreateProductViewModel = koinViewModel(),
) {
  val state = viewModel.state.collectAsStateWithLifecycle()

  CreateProductScreen(
    state = state.value,
    onNameChange = viewModel::updateName,
    onDescriptionChange = viewModel::updateDescription,
    onExpirationDateChange = viewModel::updateExpirationDate,
    onCreateClick = { viewModel.createProduct(onBack) },
    onBackClick = onBack,
    onErrorDismiss = viewModel::clearError,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProductScreen(
  state: CreateProductState,
  onNameChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onExpirationDateChange: (String) -> Unit,
  onCreateClick: () -> Unit,
  onBackClick: () -> Unit,
  onErrorDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text("Create Product") },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = AppIcons.Back,
              contentDescription = "Back"
            )
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Product Name Field
      TextField(
        value = state.name,
        onValueChange = onNameChange,
        label = { Text("Product Name") },
        placeholder = { Text("Enter product name") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading,
        isError = state.error != null,
      )

      // Product Description Field
      TextField(
        value = state.description,
        onValueChange = onDescriptionChange,
        label = { Text("Description") },
        placeholder = { Text("Enter description") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        enabled = !state.isLoading,
      )

      // Expiration Date Field
      TextField(
        value = state.expirationDateText,
        onValueChange = onExpirationDateChange,
        label = { Text("Expiration Date") },
        placeholder = { Text("DD/MM/YYYY") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading,
        isError = state.error != null,
      )

      // Error Message
      if (state.error != null) {
        Text(
          text = state.error,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
        )
        LaunchedEffect(state.error) {
          kotlinx.coroutines.delay(3000)
          onErrorDismiss()
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(
          onClick = onBackClick,
          enabled = !state.isLoading,
        ) {
          Text("Cancel")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        TextButton(
          onClick = onCreateClick,
          enabled = !state.isLoading,
        ) {
          if (state.isLoading) {
            CircularProgressIndicator()
          } else {
            Text("Create")
          }
        }
      }
    }
  }
}
