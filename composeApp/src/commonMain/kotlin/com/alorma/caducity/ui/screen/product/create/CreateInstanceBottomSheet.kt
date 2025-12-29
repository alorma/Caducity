package com.alorma.caducity.ui.screen.product.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.create_product_button_cancel
import caducity.composeapp.generated.resources.create_product_button_create
import caducity.composeapp.generated.resources.create_product_expiration_date_label
import caducity.composeapp.generated.resources.create_product_expiration_date_placeholder
import caducity.composeapp.generated.resources.create_product_instance_identifier_label
import caducity.composeapp.generated.resources.create_product_instance_identifier_placeholder
import com.alorma.caducity.barcode.BarcodeHandler
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.BarcodeScanner
import com.alorma.caducity.base.ui.theme.CaducityTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInstanceBottomSheet(
  instanceId: String?,
  instance: ProductInstanceInput?,
  onSave: (String, LocalDate) -> Unit,
  onDismiss: () -> Unit,
  barcodeHandler: BarcodeHandler,
  modifier: Modifier = Modifier,
  dateFormat: DateTimeFormat<LocalDate> = koinInject(),
  selectableDates: FutureDateSelectableDates = koinInject()
) {
  var identifier by remember(instance) { mutableStateOf(instance?.identifier ?: "") }
  var expirationDate by remember(instance) { mutableStateOf(instance?.expirationDate) }
  var expirationDateText by remember(instance) {
    mutableStateOf(
      instance?.expirationDateText ?: ""
    )
  }
  var showDatePicker by remember { mutableStateOf(false) }

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

      // Instance Identifier Field
      TextField(
        value = identifier,
        onValueChange = { identifier = it },
        label = { Text(stringResource(Res.string.create_product_instance_identifier_label)) },
        placeholder = { Text(stringResource(Res.string.create_product_instance_identifier_placeholder)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
          unfocusedIndicatorColor = CaducityTheme.colorScheme.primary,
          disabledTrailingIconColor = CaducityTheme.colorScheme.primary,
        ),
        trailingIcon = if (barcodeHandler.hasBarcodeCapability()) {
          {
            IconButton(
              onClick = {
                barcodeHandler.scan(
                  onBarcodeObtained = { barcode ->
                    identifier = barcode.data
                  },
                )
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

      // Instance Expiration Date Field
      TextField(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showDatePicker = true },
        value = expirationDateText,
        onValueChange = {},
        label = { Text(stringResource(Res.string.create_product_expiration_date_label)) },
        placeholder = { Text(stringResource(Res.string.create_product_expiration_date_placeholder)) },
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
          Text(stringResource(Res.string.create_product_button_cancel))
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
          onClick = {
            expirationDate?.let { date ->
              onSave(identifier, date)
            }
          },
          enabled = identifier.isNotBlank() && expirationDate != null
        ) {
          Text(stringResource(Res.string.create_product_button_create))
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
              Text(stringResource(Res.string.create_product_button_create))
            }
          },
          dismissButton = {
            TextButton(onClick = { showDatePicker = false }) {
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
