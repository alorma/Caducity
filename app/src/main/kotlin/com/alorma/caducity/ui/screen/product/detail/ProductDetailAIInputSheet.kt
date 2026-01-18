package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R

/**
 * Bottom sheet for AI-powered variant/instance creation from natural language
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailAIInputSheet(
  productName: String,
  onDismiss: () -> Unit,
  onGenerate: (String) -> Unit,
  isGenerating: Boolean,
  modifier: Modifier = Modifier,
  sheetState: SheetState = rememberModalBottomSheetState()
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = modifier,
  ) {
    ProductDetailAIInputContent(
      productName = productName,
      onGenerate = onGenerate,
      isGenerating = isGenerating,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    )
  }
}

@Composable
private fun ProductDetailAIInputContent(
  productName: String,
  onGenerate: (String) -> Unit,
  isGenerating: Boolean,
  modifier: Modifier = Modifier
) {
  var promptText by remember { mutableStateOf("") }
  val maxLength = 500

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Title
    Text(
      text = stringResource(R.string.product_detail_ai_title),
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Context: Show which product we're adding to
    Text(
      text = "Adding to: $productName",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Input field
    OutlinedTextField(
      value = promptText,
      onValueChange = { if (it.length <= maxLength) promptText = it },
      label = { Text(stringResource(R.string.product_detail_ai_prompt_hint)) },
      placeholder = { Text(stringResource(R.string.product_detail_ai_prompt_example)) },
      enabled = !isGenerating,
      modifier = Modifier.fillMaxWidth(),
      minLines = 3,
      maxLines = 5,
      supportingText = {
        Text(
          text = stringResource(R.string.dashboard_ai_char_counter, promptText.length),
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.fillMaxWidth()
        )
      }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Generate button
    Button(
      onClick = {
        if (promptText.isNotBlank()) {
          onGenerate(promptText)
        }
      },
      enabled = promptText.isNotBlank() && !isGenerating,
      modifier = Modifier.fillMaxWidth()
    ) {
      if (isGenerating) {
        CircularProgressIndicator(
          modifier = Modifier
            .padding(end = 8.dp)
            .height(20.dp),
          strokeWidth = 2.dp
        )
        Text(stringResource(R.string.product_detail_ai_generating))
      } else {
        Text(stringResource(R.string.product_detail_ai_generate_button))
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
