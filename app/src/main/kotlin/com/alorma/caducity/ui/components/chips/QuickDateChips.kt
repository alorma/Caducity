package com.alorma.caducity.ui.components.chips

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val scheme = CaducityTheme.colorScheme
    options.forEachIndexed { index, option ->
      FilterChip(
        selected = option.selected,
        onClick = { onClick(index) },
        shape = CaducityShapes.chip,
        modifier = Modifier.height(40.dp),
        border =
          FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = option.selected,
            borderColor = scheme.outlineVariant,
            selectedBorderColor = Color.Transparent,
          ),
        colors =
          FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = scheme.onSurface,
            selectedContainerColor = scheme.secondaryContainer,
            selectedLabelColor = scheme.onSecondaryContainer,
            iconColor = scheme.onSurface,
            selectedLeadingIconColor = scheme.onSecondaryContainer,
          ),
        leadingIcon =
          option.icon?.let {
            { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp)) }
          },
        label = { Text(option.label, style = CaducityTheme.typography.labelLarge) },
      )
    }
  }
}
