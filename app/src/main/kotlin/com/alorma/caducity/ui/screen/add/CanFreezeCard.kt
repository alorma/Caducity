package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.components.switch.CaducitySwitch
import com.alorma.caducity.ui.theme.CaducityTheme

/**
 * «Es pot congelar» switch of the product. The subtitle recalls the value of the category ([categoryName],
 * [categoryCanFreeze]) and that flipping it only affects this product.
 */
@Composable
fun CanFreezeCard(
  canFreeze: Boolean,
  categoryName: String,
  categoryCanFreeze: Boolean,
  onCanFreezeChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = CaducityTheme.shapes.large,
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    modifier =
      modifier
        .fillMaxWidth()
        .toggleable(value = canFreeze, role = Role.Switch, onValueChange = onCanFreezeChange),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Icon(
        Symbols.Outlined.AcUnit,
        contentDescription = null,
        tint = CaducityTheme.colors.frozen.onContainer,
        modifier = Modifier.size(24.dp),
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.add_can_freeze),
          style = CaducityTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
          text =
            stringResource(
              R.string.add_can_freeze_subtitle,
              categoryName,
              stringResource(if (categoryCanFreeze) R.string.add_can_freeze_yes else R.string.add_can_freeze_no),
            ),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
      CaducitySwitch(checked = canFreeze, onCheckedChange = null)
    }
  }
}
