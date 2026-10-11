package com.alorma.caducity.ui.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.softColors
import com.alorma.caducity.ui.components.feedback.vibrantColors

/**
 * Result returned from the quantity selector dialog.
 */
sealed interface QuantitySelectorResult {
  data object Dismissed : QuantitySelectorResult

  data class Selected(
    val quantity: Int,
  ) : QuantitySelectorResult
}

/**
 * Shows a quantity selector dialog for selecting how many items from a pack to apply an action to.
 *
 * @param maxQuantity The maximum quantity that can be selected (pack size)
 * @param title Composable for the dialog title
 * @param positiveButton Composable for the confirm button (receives selected quantity)
 * @param negativeButton Composable for the cancel button
 * @param type The feedback type for theming
 * @return The result of the dialog interaction
 */
suspend fun AppDialogState.showQuantitySelectorDialog(
  maxQuantity: Int,
  title: @Composable () -> Unit,
  positiveButton: @Composable (selectedQuantity: Int) -> Unit,
  negativeButton: (@Composable () -> Unit)? = null,
  type: AppFeedbackType,
): QuantitySelectorResult {
  var selectedQuantity by mutableIntStateOf(1)

  val result =
    showAlertDialog(
      title = title,
      content = {
        QuantitySelectorContent(
          selectedQuantity = selectedQuantity,
          maxQuantity = maxQuantity,
          onQuantityChanged = { selectedQuantity = it },
          type = type,
        )
      },
      positiveButton = { positiveButton(selectedQuantity) },
      negativeButton = negativeButton,
      type = type,
    )

  return when (result) {
    DialogResult.Positive -> QuantitySelectorResult.Selected(selectedQuantity)
    DialogResult.Negative, DialogResult.Dismissed -> QuantitySelectorResult.Dismissed
  }
}

/**
 * Content for the quantity selector dialog.
 * Shows increment/decrement buttons, current selection, and "All" button.
 */
@Composable
private fun QuantitySelectorContent(
  selectedQuantity: Int,
  maxQuantity: Int,
  onQuantityChanged: (Int) -> Unit,
  type: AppFeedbackType,
) {
  val colors = type.softColors()
  val vibrantColors = type.vibrantColors()

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
  ) {
    Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
      FilledIconButton(
        onClick = { if (selectedQuantity > 1) onQuantityChanged(selectedQuantity - 1) },
        enabled = selectedQuantity > 1,
        colors =
          IconButtonDefaults.filledIconButtonColors(
            containerColor = vibrantColors.container,
            contentColor = vibrantColors.onContainer,
          ),
      ) {
        val decreaseLabel = stringResource(R.string.quantity_selector_decrease)
        Text(
          text = "−",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.semantics { contentDescription = decreaseLabel },
        )
      }

      Spacer(modifier = Modifier.width(24.dp))

      Text(
        text = selectedQuantity.toString(),
        style = MaterialTheme.typography.displaySmall,
        color = colors.onContainer,
      )

      Spacer(modifier = Modifier.width(24.dp))

      FilledIconButton(
        onClick = { if (selectedQuantity < maxQuantity) onQuantityChanged(selectedQuantity + 1) },
        enabled = selectedQuantity < maxQuantity,
        colors =
          IconButtonDefaults.filledIconButtonColors(
            containerColor = vibrantColors.container,
            contentColor = vibrantColors.onContainer,
          ),
      ) {
        Icon(AppIcons.Add, contentDescription = stringResource(R.string.quantity_selector_increase))
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // "All" button to quickly select maximum
    Button(
      onClick = { onQuantityChanged(maxQuantity) },
      colors =
        ButtonDefaults.buttonColors(
          containerColor = vibrantColors.container,
          contentColor = vibrantColors.onContainer,
        ),
    ) {
      Text(stringResource(R.string.quantity_selector_all_count, maxQuantity))
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = stringResource(R.string.quantity_selector_max, maxQuantity),
      style = MaterialTheme.typography.bodySmall,
      color = colors.onContainer.copy(alpha = 0.7f),
    )
  }
}
