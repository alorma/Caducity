package com.alorma.caducity.ui.screen.product.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.barcode.base.BarcodeHandler
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.BarcodeScanner
import com.alorma.caducity.config.ConfigQualifier
import com.alorma.caducity.ui.screen.product.detail.VariantUiModel
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInstanceBottomSheet(
  instanceId: String?,
  instance: ProductInstanceInput?,
  currentGroupCount: Int = 1,
  groupExpirationDates: List<String> = emptyList(),
  availableVariants: List<VariantUiModel> = emptyList(),
  onSave: (identifier: String, variantId: String?, expirationDate: LocalDate, quantity: Int) -> Unit,
  onCreateVariant: (String, (VariantUiModel) -> Unit) -> Unit = { _, _ -> },
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  scannedBarcode: String? = null,
  dateFormat: DateTimeFormat<LocalDate> = koinInject(qualifier = ConfigQualifier.DateFormat.HumanReadable),
  selectableDates: FutureDateSelectableDates = koinInject(),
  barcodeHandler: BarcodeHandler = koinInject(),
) {
  var useVariant by remember { mutableStateOf(availableVariants.isNotEmpty()) }
  var selectedVariantId by remember { mutableStateOf<String?>(null) }
  var identifier by remember(instance, scannedBarcode) {
    mutableStateOf(scannedBarcode ?: instance?.identifier ?: "")
  }
  var expirationDate by remember(instance) { mutableStateOf(instance?.expirationDate) }
  var expirationDateText by remember(instance) {
    mutableStateOf(
      instance?.expirationDateText ?: ""
    )
  }
  var quantity by remember(instanceId, currentGroupCount) {
    mutableStateOf(if (instanceId != null) currentGroupCount.toString() else "1")
  }
  var showDatePicker by remember { mutableStateOf(false) }
  var showVariantDropdown by remember { mutableStateOf(false) }
  var showCreateVariantDialog by remember { mutableStateOf(false) }

  // Register permission contract for barcode scanning
  barcodeHandler.registerPermissionContract()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Title
      Text(
        text = if (instanceId == null) "Add Instance" else "Edit Instance",
        style = CaducityTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      val coroutineScope = rememberCoroutineScope()

      // Mode selection: Variant or Standalone (only show when creating new instances)
      if (instanceId == null && availableVariants.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = useVariant,
            onClick = { useVariant = true },
            label = { Text("Use Variant") }
          )
          FilterChip(
            selected = !useVariant,
            onClick = {
              useVariant = false
              selectedVariantId = null
            },
            label = { Text("Standalone") }
          )
        }
      }

      // Variant dropdown (when useVariant is true)
      if (useVariant && instanceId == null) {
        ExposedDropdownMenuBox(
          expanded = showVariantDropdown,
          onExpandedChange = { showVariantDropdown = it }
        ) {
          val selectedVariant = availableVariants.find { it.id == selectedVariantId }

          TextField(
            value = selectedVariant?.name ?: "Select variant",
            onValueChange = {},
            readOnly = true,
            label = { Text("Variant") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showVariantDropdown) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            colors = TextFieldDefaults.colors()
          )

          ExposedDropdownMenu(
            expanded = showVariantDropdown,
            onDismissRequest = { showVariantDropdown = false }
          ) {
            // Existing variants
            availableVariants.forEach { variant ->
              DropdownMenuItem(
                text = { Text(variant.name) },
                onClick = {
                  selectedVariantId = variant.id
                  identifier = ""  // Empty identifier for variant-based instances
                  showVariantDropdown = false
                }
              )
            }

            HorizontalDivider()

            // Create new variant option
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(AppIcons.Add, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text("Create new variant")
                }
              },
              onClick = {
                showVariantDropdown = false
                showCreateVariantDialog = true
              }
            )
          }
        }
      }

      // Standalone identifier field (when useVariant is false or when editing)
      if (!useVariant || instanceId != null) {
        // Instance Identifier Field
        TextField(
        value = identifier,
        onValueChange = { identifier = it },
        label = { Text(stringResource(R.string.create_product_instance_identifier_label)) },
        placeholder = { Text(stringResource(R.string.create_product_instance_identifier_placeholder)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
          unfocusedIndicatorColor = CaducityTheme.colorScheme.primary,
          disabledTrailingIconColor = CaducityTheme.colorScheme.primary,
        ),
        trailingIcon = if (barcodeHandler.hasBarcodeCapability()) {
          {
            IconButton(
              onClick = {
                coroutineScope.launch {
                  barcodeHandler.scan(
                    onBarcodeObtained = { barcode ->
                      identifier = barcode.data
                    },
                  )
                }
              },
            ) {
              Icon(
                imageVector = AppIcons.BarcodeScanner,
                contentDescription = null,
              )
            }
          }
        } else {
          null
        },
      )
      }

      // Quantity Field - only show when creating new instance
      if (instanceId == null) {
        TextField(
          value = quantity,
          onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
              quantity = newValue
            }
          },
          label = { Text(stringResource(R.string.create_product_instance_quantity_label)) },
          placeholder = { Text(stringResource(R.string.create_product_instance_quantity_placeholder)) },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
        )

        // Preview text when quantity > 1
        val quantityValue = quantity.toIntOrNull() ?: 1
        if (quantityValue > 1) {
          Text(
            text = stringResource(R.string.create_product_instance_split_preview, quantityValue),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp)
          )
        }
      }

      // Instance Expiration Date Field
      TextField(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showDatePicker = true },
        value = expirationDateText,
        onValueChange = {},
        label = { Text(stringResource(R.string.create_product_expiration_date_label)) },
        placeholder = { Text(stringResource(R.string.create_product_expiration_date_placeholder)) },
        enabled = false,
        readOnly = true,
        colors = TextFieldDefaults.colors(
          focusedContainerColor = TextFieldDefaults.colors().containerColor(
            enabled = true,
            isError = false,
            focused = true,
          ),
          unfocusedContainerColor = TextFieldDefaults.colors().containerColor(
            enabled = true,
            isError = false,
            focused = true,
          ),
          disabledTextColor = CaducityTheme.colorScheme.onSurface,
          disabledContainerColor = TextFieldDefaults.colors().containerColor(
            enabled = true,
            isError = false,
            focused = false,
          ),
          disabledLabelColor = CaducityTheme.colorScheme.onSurfaceVariant,
        ),
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(onClick = onDismiss) {
          Text(stringResource(R.string.create_product_button_cancel))
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
          onClick = {
            expirationDate?.let { date ->
              val quantityValue = quantity.toIntOrNull()?.coerceIn(1, 99) ?: 1
              val finalVariantId = if (useVariant) selectedVariantId else null
              onSave(identifier, finalVariantId, date, quantityValue)
            }
          },
          enabled = (useVariant || identifier.isNotBlank()) && expirationDate != null &&
              (quantity.toIntOrNull() ?: 0) in 1..99 &&
              (!useVariant || selectedVariantId != null || instanceId != null)
        ) {
          Text(stringResource(R.string.create_product_button_create))
        }
      }

      // Date Picker Dialog
      if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
          selectableDates = selectableDates
        )

        DatePickerDialog(
          onDismissRequest = { showDatePicker = false },
          confirmButton = {
            TextButton(
              onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                  val instant = Instant.fromEpochMilliseconds(millis)
                  val localDateTime = instant.toLocalDateTime(TimeZone.UTC)
                  expirationDate = localDateTime.date
                  expirationDateText = dateFormat.format(localDateTime.date)
                }
                showDatePicker = false
              }
            ) {
              Text(stringResource(R.string.create_product_button_create))
            }
          },
          dismissButton = {
            TextButton(onClick = { showDatePicker = false }) {
              Text(stringResource(R.string.create_product_button_cancel))
            }
          }
        ) {
          DatePicker(state = datePickerState)
        }
      }

      // Create Variant Dialog
      if (showCreateVariantDialog) {
        var newVariantName by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
          onDismissRequest = { showCreateVariantDialog = false },
          title = { Text("Create New Variant") },
          text = {
            TextField(
              value = newVariantName,
              onValueChange = { newVariantName = it },
              label = { Text("Variant Name") },
              placeholder = { Text("e.g., Strawberry, Greek, Low-fat") },
              modifier = Modifier.fillMaxWidth()
            )
          },
          confirmButton = {
            Button(
              onClick = {
                if (newVariantName.isNotBlank()) {
                  onCreateVariant(newVariantName) { newVariant ->
                    selectedVariantId = newVariant.id
                    identifier = ""  // Empty identifier for variant-based instances
                    showCreateVariantDialog = false
                  }
                }
              },
              enabled = newVariantName.isNotBlank()
            ) {
              Text("Create")
            }
          },
          dismissButton = {
            TextButton(onClick = { showCreateVariantDialog = false }) {
              Text("Cancel")
            }
          }
        )
      }
    }
  }
}
