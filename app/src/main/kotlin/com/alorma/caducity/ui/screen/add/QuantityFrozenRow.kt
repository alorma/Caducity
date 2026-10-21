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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.unit.formatAmount

/** Connected group of 64 dp: quantity stepper and the «Congelat» toggle. */
@Composable
fun QuantityFrozenRow(
  quantity: Int,
  unitType: UnitType = UnitType.DEFAULT,
  amount: Int = UnitType.DEFAULT_AMOUNT,
  onAmountChange: (Int) -> Unit = {},
  frozen: Boolean,
  onQuantityChange: (Int) -> Unit,
  onFrozenChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  /** False hides the «Congelat» toggle: the product cannot be frozen. */
  canFreeze: Boolean = true,
) {
  val step = unitType.amountStep.takeIf { unitType.parent == UnitParent.MEASURED }
  Row(
    modifier = modifier.fillMaxWidth().height(64.dp),
    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
  ) {
    Surface(
      shape = if (canFreeze) ButtonGroupDefaults.connectedLeadingButtonShape else CaducityTheme.shapes.large,
      color = CaducityTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.weight(if (step != null) 2.5f else 2f).height(64.dp),
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
          enabled = if (step != null) amount > step else quantity > 1,
          onClick = {
            if (step !=
              null
            ) {
              onAmountChange(maxOf(step, amount - step))
            } else {
              onQuantityChange(quantity - 1)
            }
          },
          label = stringResource(R.string.add_quantity_decrease),
        ) {
          Text("−", style = CaducityTheme.typography.titleMedium)
        }
        Text(
          text =
            if (step !=
              null
            ) {
              formatAmount(unitType, amount, LocalConfiguration.current.locales[0])
            } else {
              quantity.toString()
            },
          style = CaducityTheme.typography.titleMedium,
          textAlign = TextAlign.Center,
          maxLines = 1,
          modifier = Modifier.width(if (step != null) 72.dp else 36.dp),
        )
        StepButton(
          enabled = if (step != null) amount < UnitType.MAX_AMOUNT else quantity < AddProductUseCase.MAX_QUANTITY,
          onClick = {
            if (step !=
              null
            ) {
              onAmountChange(minOf(UnitType.MAX_AMOUNT, amount + step))
            } else {
              onQuantityChange(quantity + 1)
            }
          },
          label = stringResource(R.string.add_quantity_increase),
        ) {
          Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        }
      }
    }
    if (!canFreeze) return@Row
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
