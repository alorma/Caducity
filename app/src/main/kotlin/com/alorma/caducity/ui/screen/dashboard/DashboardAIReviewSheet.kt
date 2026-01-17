package com.alorma.caducity.ui.screen.dashboard

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
import com.alorma.caducity.feature.fakedata.models.MatchingResults
import com.alorma.caducity.feature.fakedata.models.ProductMatchResult

/**
 * Bottom sheet for reviewing matched products before adding
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAIReviewSheet(
  matchingResults: MatchingResults,
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
    DashboardAIReviewContent(
      matchingResults = matchingResults,
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    )
  }
}

@Composable
private fun DashboardAIReviewContent(
  matchingResults: MatchingResults,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Title
    Text(
      text = stringResource(R.string.dashboard_ai_review_title),
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.fillMaxWidth()
    )

    // Description
    Text(
      text = stringResource(
        R.string.dashboard_ai_review_description,
        matchingResults.perfectMatches.size
      ),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth()
    )

    // Matched products list
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f, fill = false),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(matchingResults.perfectMatches) { match ->
        MatchedProductCard(match = match)
      }

      // Show new products that will be created
      if (matchingResults.noMatches.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = stringResource(R.string.dashboard_ai_review_new_products),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
          )
        }

        items(matchingResults.noMatches) { noMatch ->
          NewProductCard(productName = noMatch.generatedProduct.name)
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
        Text(stringResource(R.string.dashboard_ai_review_cancel))
      }

      Button(
        onClick = onConfirm,
        modifier = Modifier.weight(1f)
      ) {
        Text(stringResource(R.string.dashboard_ai_review_confirm))
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun MatchedProductCard(
  match: ProductMatchResult.PerfectMatch,
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
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = match.existingProduct.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.weight(1f)
        )

        Text(
          text = stringResource(
            R.string.dashboard_ai_review_match_score,
            (match.matchScore * 100).toInt()
          ),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer
        )
      }

      Text(
        text = stringResource(
          R.string.dashboard_ai_review_will_add,
          match.generatedProduct.variants.size,
          match.generatedProduct.variants.sumOf { it.instances.size } +
            match.generatedProduct.standaloneInstances.size
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer
      )
    }
  }
}

@Composable
private fun NewProductCard(
  productName: String,
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
        text = productName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
      )

      Text(
        text = stringResource(R.string.dashboard_ai_review_new_product),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
