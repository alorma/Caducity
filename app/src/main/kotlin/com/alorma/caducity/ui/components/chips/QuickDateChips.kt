package com.alorma.caducity.ui.components.chips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

data class QuickDateOption(
  val label: String,
  val icon: ImageVector? = null,
)

/** Dates ràpides en una sola fila amb scroll horitzontal: xips de radi 12 sobre `surface-high`. */
@Composable
fun QuickDateChips(
  options: List<QuickDateOption>,
  onClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    options.forEachIndexed { index, option ->
      Surface(
        shape = CaducityShapes.chip,
        color = CaducityTheme.colorScheme.surfaceContainerHigh,
        contentColor = CaducityTheme.colorScheme.onSurface,
        modifier = Modifier.clickable(role = Role.Button, onClick = { onClick(index) }),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          option.icon?.let {
            Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp))
          }
          Text(option.label, style = CaducityTheme.typography.labelLarge)
        }
      }
    }
  }
}
