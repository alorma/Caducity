package com.alorma.caducity.ui.components.chips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

data class QuickDateOption(
  val label: String,
  val icon: ImageVector? = null,
  val selected: Boolean = false,
)

/** Dates ràpides en una sola fila amb scroll horitzontal: xips outlined de radi 12. */
@Composable
fun QuickDateChips(
  options: List<QuickDateOption>,
  onClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
) {
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()).padding(contentPadding),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    options.forEachIndexed { index, option ->
      Surface(
        onClick = { onClick(index) },
        shape = CaducityShapes.chip,
        color = if (option.selected) CaducityTheme.colorScheme.secondaryContainer else Color.Transparent,
        border = if (option.selected) null else BorderStroke(1.dp, CaducityTheme.colorScheme.outlineVariant),
        contentColor =
          if (option.selected) CaducityTheme.colorScheme.onSecondaryContainer else CaducityTheme.colorScheme.onSurface,
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
