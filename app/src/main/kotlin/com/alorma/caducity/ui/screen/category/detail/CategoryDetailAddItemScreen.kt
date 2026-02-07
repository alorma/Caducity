package com.alorma.caducity.ui.screen.category.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Check
import com.alorma.caducity.ui.components.loading.WavyLoadingIndicator
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailAddItemScreen(
  categoryId: String,
  productId: String?,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CategoryDetailAddItemViewModel = koinViewModel { parametersOf(categoryId, productId) }
) {
  val state = viewModel.state.collectAsStateWithLifecycle()
  val formState = viewModel.formState.collectAsStateWithLifecycle()

  AppScaffold(
    modifier = modifier,
    topBar = {
      StyledTopAppBar(
        title = { Text(text = stringResource(R.string.category_detail_add_item_title)) },
        navigationIcon = { NavigationIcon() },
        actions = {
          IconButton(
            onClick = {
              viewModel.save(onSuccess = onClose)
            }
          ) {
            Icon(
              imageVector = AppIcons.Check,
              contentDescription = stringResource(R.string.category_detail_add_item_save),
            )
          }
        },
      )
    },
  ) { paddingValues ->
    when (val currentState = state.value) {
      is CategoryDetailAddItemState.Loading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
          contentAlignment = Alignment.Center,
        ) {
          WavyLoadingIndicator()
        }
      }

      is CategoryDetailAddItemState.Success -> {
        var expanded by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }

        ResponsiveCenteredContainer(
          maxWidth = 600.dp,
          modifier = Modifier.padding(paddingValues),
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 24.dp)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
          // Variant selection with filter
          ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
          ) {
            TextField(
              value = formState.value.productText.text,
              onValueChange = { viewModel.onProductTextChanged(TextFieldValue(it)) },
              label = { Text(stringResource(R.string.category_detail_add_item_product_label)) },
              placeholder = { Text(stringResource(R.string.category_detail_add_item_product_placeholder)) },
              trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
              },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
              singleLine = true,
            )

            val filteredProducts = viewModel.getFilteredProducts()
            if (filteredProducts.isNotEmpty()) {
              ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
              ) {
                filteredProducts.forEach { product ->
                  DropdownMenuItem(
                    text = { Text(product.name) },
                    onClick = {
                      viewModel.onProductSelected(product.id, product.name)
                      expanded = false
                    },
                  )
                }
              }
            }
          }

          // Identifier field
          TextField(
            value = formState.value.identifierText.text,
            onValueChange = { viewModel.onIdentifierTextChanged(TextFieldValue(it)) },
            label = { Text(stringResource(R.string.category_detail_add_item_identifier_label)) },
            placeholder = { Text(stringResource(R.string.category_detail_add_item_identifier_placeholder)) },
            isError = formState.value.identifierError != null,
            supportingText = formState.value.identifierError?.let { error ->
              { Text(text = error) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
          )

          // Expiration date field
          TextField(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showDatePicker = true },
            value = formState.value.expirationDateMillis?.let {
              val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
              dateFormat.format(Date(it))
            } ?: "",
            onValueChange = { },
            readOnly = true,
            enabled = false,
            label = { Text(stringResource(R.string.category_detail_add_item_expiration_date_label)) },
            placeholder = { Text(stringResource(R.string.category_detail_add_item_expiration_date_placeholder)) },
            isError = formState.value.expirationDateError != null,
            supportingText = formState.value.expirationDateError?.let { error ->
              { Text(text = error) }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
              disabledTextColor = MaterialTheme.colorScheme.onSurface,
              disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
              disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
              disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
              disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
          )

          // Quantity controls
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = stringResource(R.string.category_detail_add_item_quantity_label),
              style = MaterialTheme.typography.bodyLarge,
            )

            // Quick selection chips (1-6 + More)
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              // Chips for quantities 1-6
              (1..6).forEach { num ->
                FilterChip(
                  selected = formState.value.quantity == num && !formState.value.showCustomQuantityInput,
                  onClick = {
                    viewModel.onQuantityChanged(num)
                    viewModel.onShowCustomQuantityInputChanged(false)
                  },
                  label = { Text(text = num.toString()) },
                )
              }

              // "More" chip to reveal custom input
              FilterChip(
                selected = formState.value.showCustomQuantityInput,
                onClick = {
                  viewModel.onShowCustomQuantityInputChanged(!formState.value.showCustomQuantityInput)
                },
                label = { Text(text = stringResource(R.string.category_detail_add_item_quantity_more)) },
              )
            }

            // Custom quantity TextField (shown when "More" is selected)
            if (formState.value.showCustomQuantityInput) {
              TextField(
                value = formState.value.customQuantity.text,
                onValueChange = { viewModel.onCustomQuantityChanged(TextFieldValue(it)) },
                label = { Text(stringResource(R.string.category_detail_add_item_quantity_custom_label)) },
                placeholder = { Text("7") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
              )
            }
          }

          if (showDatePicker) {
            ExpirationDatePickerDialog(
              initialDateMillis = formState.value.expirationDateMillis,
              onDateSelected = { dateMillis ->
                viewModel.onExpirationDateChanged(dateMillis)
                showDatePicker = false
              },
              onDismiss = { showDatePicker = false }
            )
          }
          }
        }
      }

      is CategoryDetailAddItemState.Error -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpirationDatePickerDialog(
  initialDateMillis: Long?,
  onDateSelected: (Long?) -> Unit,
  onDismiss: () -> Unit,
) {
  val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = initialDateMillis
  )

  val confirmEnabled by remember {
    derivedStateOf { datePickerState.selectedDateMillis != null }
  }

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          onDateSelected(datePickerState.selectedDateMillis)
        },
        enabled = confirmEnabled
      ) {
        Text(stringResource(R.string.category_detail_add_item_date_picker_ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.category_detail_add_item_date_picker_cancel))
      }
    }
  ) {
    DatePicker(state = datePickerState)
  }
}
