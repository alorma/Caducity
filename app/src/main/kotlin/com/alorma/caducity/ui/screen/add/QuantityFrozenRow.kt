package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.usecase.AddProductUseCase
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
    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
  ) {
    Surface(
      shape = ButtonGroupDefaults.connectedLeadingButtonShape,
      color = CaducityTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.weight(2f).height(64.dp),
    ) {
      Row(
        modifier = Modifier.padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.add_quantity),
          style = CaducityTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
          maxLines = 1,
          softWrap = false,
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
          modifier = Modifier.width(36.dp),
        )
        StepButton(
          enabled = quantity < AddProductUseCase.MAX_QUANTITY,
          onClick = { onQuantityChange(quantity + 1) },
          label = stringResource(R.string.add_quantity_increase),
        ) {
          Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        }
      }
    }
    val frozenColors = CaducityTheme.colors.frozen
    ToggleButton(
      checked = frozen,
      onCheckedChange = onFrozenChange,
      modifier = Modifier.weight(1f).height(64.dp),
      shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
      colors =
        ToggleButtonDefaults.colors(
          containerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
          contentColor = CaducityTheme.colorScheme.onSurface,
          checkedContainerColor = frozenColors.container,
          checkedContentColor = frozenColors.onContainer,
        ),
      contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
      Icon(Symbols.Outlined.AcUnit, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(6.dp))
      Text(stringResource(R.string.add_frozen), maxLines = 1)
    }
  }
}

@Composable
internal fun StepButton(
  enabled: Boolean,
  onClick: () -> Unit,
  label: String,
  content: @Composable () -> Unit,
) {
  FilledIconButton(
    onClick = onClick,
    enabled = enabled,
    shape = CaducityTheme.shapes.small,
    colors =
      IconButtonDefaults.filledIconButtonColors(
        containerColor = CaducityTheme.colorScheme.surface,
        contentColor = CaducityTheme.colorScheme.onSurface,
        disabledContainerColor = CaducityTheme.colorScheme.surface,
        disabledContentColor = CaducityTheme.colorScheme.outline,
      ),
    modifier = Modifier.size(44.dp).semantics { contentDescription = label },
  ) {
    content()
  }
}
