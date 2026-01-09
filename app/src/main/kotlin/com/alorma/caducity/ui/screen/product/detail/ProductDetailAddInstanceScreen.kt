package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Check
import com.alorma.caducity.ui.components.NavigationIcon
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.ui.components.loading.WavyLoadingIndicator
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailAddInstanceScreen(
  productId: String,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductDetailAddInstanceViewModel = koinViewModel { parametersOf(productId) }
) {
  val state = viewModel.state.collectAsStateWithLifecycle()
  val formState = viewModel.formState.collectAsStateWithLifecycle()

  AppScaffold(
    modifier = modifier,
    topBar = {
      StyledTopAppBar(
        title = { Text(text = stringResource(R.string.product_detail_add_instance_title)) },
        navigationIcon = { NavigationIcon() },
        actions = {
          IconButton(
            onClick = {
              viewModel.save(onSuccess = onClose)
            }
          ) {
            Icon(
              imageVector = AppIcons.Check,
              contentDescription = stringResource(R.string.product_detail_add_instance_save),
            )
          }
        },
      )
    },
  ) { paddingValues ->
    when (val currentState = state.value) {
      is ProductDetailAddInstanceState.Loading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
          contentAlignment = Alignment.Center,
        ) {
          WavyLoadingIndicator()
        }
      }

      is ProductDetailAddInstanceState.Success -> {
        var expanded by remember { mutableStateOf(false) }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Variant selection with filter
          ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
          ) {
            OutlinedTextField(
              value = formState.value.variantText,
              onValueChange = { viewModel.onVariantTextChanged(it) },
              shape = CaducityTheme.shapes.medium,
              label = { Text(stringResource(R.string.product_detail_add_instance_variant_label)) },
              placeholder = { Text(stringResource(R.string.product_detail_add_instance_variant_placeholder)) },
              trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
              },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
              singleLine = true,
            )

            val filteredVariants = viewModel.getFilteredVariants()
            if (filteredVariants.isNotEmpty()) {
              ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
              ) {
                filteredVariants.forEach { variant ->
                  DropdownMenuItem(
                    text = { Text(variant.name) },
                    onClick = {
                      viewModel.onVariantSelected(variant.id, variant.name)
                      expanded = false
                    },
                  )
                }
              }
            }
          }

          // Identifier field
          OutlinedTextField(
            value = formState.value.identifierText,
            onValueChange = { viewModel.onIdentifierTextChanged(it) },
            shape = CaducityTheme.shapes.medium,
            label = { Text(stringResource(R.string.product_detail_add_instance_identifier_label)) },
            placeholder = { Text(stringResource(R.string.product_detail_add_instance_identifier_placeholder)) },
            isError = formState.value.identifierError != null,
            supportingText = formState.value.identifierError?.let { error ->
              { Text(text = error) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
          )
        }
      }

      is ProductDetailAddInstanceState.Error -> {
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
