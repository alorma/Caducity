package com.alorma.caducity.ui.screen.product.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.create_product_button_cancel
import caducity.composeapp.generated.resources.create_product_button_create
import caducity.composeapp.generated.resources.create_product_content_description_back
import caducity.composeapp.generated.resources.create_product_description_label
import caducity.composeapp.generated.resources.create_product_description_placeholder
import caducity.composeapp.generated.resources.create_product_expiration_date_label
import caducity.composeapp.generated.resources.create_product_expiration_date_placeholder
import caducity.composeapp.generated.resources.create_product_name_label
import caducity.composeapp.generated.resources.create_product_name_placeholder
import caducity.composeapp.generated.resources.create_product_screen_title
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@Composable
fun CreateProductScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CreateProductViewModel = koinViewModel(),
) {
  val state = viewModel.state.collectAsStateWithLifecycle()

  CreateProductPage(
    state = state.value,
    onNameChange = viewModel::updateName,
    onDescriptionChange = viewModel::updateDescription,
    onExpirationDateChange = viewModel::updateExpirationDate,
    onShowDatePicker = viewModel::showDatePicker,
    onHideDatePicker = viewModel::hideDatePicker,
    onCreateClick = { viewModel.createProduct(onBack) },
    onBackClick = onBack,
    onErrorDismiss = viewModel::clearError,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProductPage(
  state: CreateProductState,
  onNameChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onExpirationDateChange: (LocalDate) -> Unit,
  onShowDatePicker: () -> Unit,
  onHideDatePicker: () -> Unit,
  onCreateClick: () -> Unit,
  onBackClick: () -> Unit,
  onErrorDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text(stringResource(Res.string.create_product_screen_title)) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = AppIcons.Back,
              contentDescription = stringResource(Res.string.create_product_content_description_back)
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

      // Expiration Date Field
      OutlinedTextField(
        value = state.expirationDateText ?: "",
        onValueChange = {},
        label = { Text(stringResource(Res.string.create_product_expiration_date_label)) },
        placeholder = { Text(stringResource(Res.string.create_product_expiration_date_placeholder)) },
        modifier = Modifier
          .fillMaxWidth()
          .clickable(enabled = !state.isLoading) { onShowDatePicker() },
        enabled = false,
        readOnly = true,
        isError = state.error != null,
        colors = OutlinedTextFieldDefaults.colors(
          disabledTextColor = MaterialTheme.colorScheme.onSurface,
          disabledBorderColor = MaterialTheme.colorScheme.outline,
          disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      )

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
          Text(stringResource(Res.string.create_product_button_cancel))
        }
        Spacer(modifier = Modifier.padding(8.dp))
        TextButton(
          onClick = onCreateClick,
          enabled = !state.isLoading,
        ) {
          if (state.isLoading) {
            CircularProgressIndicator()
          } else {
            Text(stringResource(Res.string.create_product_button_create))
          }
        }
      }

      // Date Picker Dialog
      if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
          onDismissRequest = onHideDatePicker,
          confirmButton = {
            TextButton(
              onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                  // Convert epoch millis to LocalDate
                  val instant = Instant.fromEpochMilliseconds(millis)
                  val localDateTime = instant.toLocalDateTime(TimeZone.UTC)
                  onExpirationDateChange(localDateTime.date)
                }
                onHideDatePicker()
              }
            ) {
              Text(stringResource(Res.string.create_product_button_create))
            }
          },
          dismissButton = {
            TextButton(onClick = onHideDatePicker) {
              Text(stringResource(Res.string.create_product_button_cancel))
            }
          }
        ) {
          DatePicker(state = datePickerState)
        }
      }
    }
  }
}
