package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.usecase.AddItemBatchUseCase
import com.alorma.caducity.ui.components.button.ConnectedButtonSpacing
import com.alorma.caducity.ui.components.button.connectedButtonShape
import com.alorma.caducity.ui.theme.CaducityTheme

/** Connected group of 64 dp: quantity stepper and the «Congelat» toggle. */
@Composable
fun QuantityFrozenRow(
  quantity: Int,
  frozen: Boolean,
  onQuantityChange: (Int) -> Unit,
  onFrozenChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().height(64.dp),
    horizontalArrangement = Arrangement.spacedBy(ConnectedButtonSpacing),
  ) {
    Surface(
      shape = connectedButtonShape(0, 2),
      color = CaducityTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.weight(1.4f).height(64.dp),
    ) {
      Row(
        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.add_quantity),
          style = CaducityTheme.typography.labelLarge,
          modifier = Modifier.weight(1f),
        )
        StepButton(
          enabled = quantity > 1,
          onClick = { onQuantityChange(quantity - 1) },
          label = stringResource(R.string.add_quantity_decrease),
        ) {
          Text("−", style = CaducityTheme.typography.titleMedium)
        }
        Text(
          text = quantity.toString(),
          style = CaducityTheme.typography.titleMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier.width(32.dp),
        )
        StepButton(
          enabled = quantity < AddItemBatchUseCase.MAX_QUANTITY,
          onClick = { onQuantityChange(quantity + 1) },
          label = stringResource(R.string.add_quantity_increase),
        ) {
          Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        }
      }
    }
    val frozenColors = CaducityTheme.colors.frozen
    Surface(
      checked = frozen,
      onCheckedChange = onFrozenChange,
      shape = connectedButtonShape(1, 2),
      color = if (frozen) frozenColors.container else CaducityTheme.colorScheme.surfaceContainerHigh,
      contentColor = if (frozen) frozenColors.onContainer else CaducityTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f).height(64.dp),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(Symbols.Outlined.AcUnit, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(stringResource(R.string.add_frozen), style = CaducityTheme.typography.labelLarge, maxLines = 1)
      }
    }
  }
}

@Composable
private fun StepButton(
  enabled: Boolean,
  onClick: () -> Unit,
  label: String,
  content: @Composable () -> Unit,
) {
  Surface(
    onClick = onClick,
    enabled = enabled,
    shape = CaducityTheme.shapes.small,
    color = CaducityTheme.colorScheme.surface,
    contentColor = if (enabled) CaducityTheme.colorScheme.onSurface else CaducityTheme.colorScheme.outline,
    modifier = Modifier.size(44.dp).semantics { contentDescription = label },
  ) {
    Box(contentAlignment = Alignment.Center) { content() }
  }
}
