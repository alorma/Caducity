package com.alorma.caducity.ui.components.switch

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.caducity.ui.theme.CaducityTheme

/** Interruptor: activat `primary`/`on-primary`; apagat, vora i ull `outline` sobre `surface-high`. */
@Composable
fun CaducitySwitch(
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val scheme = CaducityTheme.colorScheme
  Switch(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
    colors =
      SwitchDefaults.colors(
        checkedThumbColor = scheme.onPrimary,
        checkedTrackColor = scheme.primary,
        checkedBorderColor = scheme.primary,
        uncheckedThumbColor = scheme.outline,
        uncheckedTrackColor = scheme.surfaceContainerHigh,
        uncheckedBorderColor = scheme.outline,
      ),
  )
}
