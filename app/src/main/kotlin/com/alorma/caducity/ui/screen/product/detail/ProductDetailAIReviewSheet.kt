package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.feature.fakedata.models.GeneratedProductVariants

/**
 * Bottom sheet for reviewing generated variants/instances before adding to product
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailAIReviewSheet(
  generatedVariants: GeneratedProductVariants,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
  sheetState: SheetState = rememberModalBottomSheetState()
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = modifier,
  ) {
    ProductDetailAIReviewContent(
      generatedVariants = generatedVariants,
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    )
  }
}

@Composable
private fun ProductDetailAIReviewContent(
  generatedVariants: GeneratedProductVariants,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier
) {
  val totalInstances = generatedVariants.variants.sumOf { it.instances.size } +
    generatedVariants.standaloneInstances.size

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Title
    Text(
      text = stringResource(R.string.product_detail_ai_review_title),
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.fillMaxWidth()
    )

    // Description
    Text(
      text = stringResource(
        R.string.product_detail_ai_review_description,
        generatedVariants.variants.size,
        totalInstances
      ),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth()
    )

    // Variants and instances list
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f, fill = false),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Show variants
      items(generatedVariants.variants) { variant ->
        VariantCard(
          variantName = variant.name,
          instanceCount = variant.instances.size
        )
      }

      // Show standalone instances
      if (generatedVariants.standaloneInstances.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Standalone Instances",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
          )
        }

        item {
          StandaloneInstancesCard(
            count = generatedVariants.standaloneInstances.size
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Action buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.weight(1f)
      ) {
        Text(stringResource(R.string.product_detail_ai_review_cancel))
      }

      Button(
        onClick = onConfirm,
        modifier = Modifier.weight(1f)
      ) {
        Text(stringResource(R.string.product_detail_ai_review_confirm))
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun VariantCard(
  variantName: String,
  instanceCount: Int,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = variantName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSecondaryContainer
      )

      Text(
        text = "$instanceCount instance${if (instanceCount != 1) "s" else ""}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer
      )
    }
  }
}

@Composable
private fun StandaloneInstancesCard(
  count: Int,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Text(
        text = "$count instance${if (count != 1) "s" else ""} without variant",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
