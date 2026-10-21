package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.ui.theme.CaducityTheme

/**
 * Connected group of a handful product (Grapats): «Queda» with Molt / Meitat / Poc, and under it the «Congelat» toggle
 * (there is no room for both side by side, as with the quantity stepper).
 */
@Composable
fun LevelFrozenRow(
  level: HandfulLevel,
  frozen: Boolean,
  onLevelChange: (HandfulLevel) -> Unit,
  onFrozenChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  /** False hides the «Congelat» toggle: the product cannot be frozen. */
  canFreeze: Boolean = true,
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Surface(
      shape = if (canFreeze) ButtonGroupDefaults.connectedLeadingButtonShape else CaducityTheme.shapes.large,
      color = CaducityTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.fillMaxWidth().height(64.dp),
    ) {
      Row(
        modifier = Modifier.padding(start = 20.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = stringResource(R.string.handful_left_label),
          style = CaducityTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier.weight(1f),
        )
        LevelSelector(level = level, onChange = onLevelChange)
      }
    }
    if (canFreeze) {
      val frozenColors = CaducityTheme.colors.frozen
      ToggleButton(
        checked = frozen,
        onCheckedChange = onFrozenChange,
        modifier = Modifier.fillMaxWidth().height(56.dp),
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
}

/** Molt / Meitat / Poc: how much is left of a handful product. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LevelSelector(
  level: HandfulLevel,
  onChange: (HandfulLevel) -> Unit,
  modifier: Modifier = Modifier,
) {
  SingleChoiceSegmentedButtonRow(modifier = modifier) {
    HandfulLevel.entries.forEachIndexed { index, option ->
      SegmentedButton(
        selected = option == level,
        onClick = { onChange(option) },
        shape = SegmentedButtonDefaults.itemShape(index = index, count = HandfulLevel.entries.size),
        icon = {},
        label = {
          Text(
            text = stringResource(option.shortLabelRes()),
            maxLines = 1,
            style = CaducityTheme.typography.labelLarge,
          )
        },
        colors =
          SegmentedButtonDefaults.colors(
            activeContainerColor = CaducityTheme.colorScheme.primary,
            activeContentColor = CaducityTheme.colorScheme.onPrimary,
            inactiveContainerColor = CaducityTheme.colorScheme.surface,
          ),
      )
    }
  }
}

@androidx.annotation.StringRes
private fun HandfulLevel.shortLabelRes(): Int =
  when (this) {
    HandfulLevel.LOTS -> R.string.handful_level_lots
    HandfulLevel.HALF -> R.string.handful_level_half_short
    HandfulLevel.LITTLE -> R.string.handful_level_little
  }
