package com.alorma.caducity.ui.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors

/** Opció de filtre. `count` es mostra en negreta darrere l'etiqueta; `status` afegeix el punt fort de l'estat. */
data class FilterChipOption(
  val label: String,
  val count: Int? = null,
  val status: ExpiryStatus? = null,
)

/** Xips de filtre d'una sola selecció (radi 12). `selectedIndex = null` no en marca cap. */
@Composable
fun FilterChips(
  options: List<FilterChipOption>,
  selectedIndex: Int?,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
) {
  val scheme = CaducityTheme.colorScheme
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()).padding(contentPadding),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    options.forEachIndexed { index, option ->
      FilterChip(
        selected = index == selectedIndex,
        onClick = { onSelect(index) },
        shape = CaducityShapes.chip,
        border =
          FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = index == selectedIndex,
            borderColor = scheme.outlineVariant,
            selectedBorderColor = Color.Transparent,
          ),
        colors =
          FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = scheme.onSurface,
            selectedContainerColor = scheme.primary,
            selectedLabelColor = scheme.onPrimary,
            selectedLeadingIconColor = scheme.onPrimary,
          ),
        leadingIcon =
          when {
            index == selectedIndex -> {
              { Icon(Symbols.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
            }
            option.status != null -> {
              {
                Spacer(
                  Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(option.status.colors().strong),
                )
              }
            }
            else -> null
          },
        label = {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(option.label, style = CaducityTheme.typography.labelLarge)
            option.count?.let {
              Text(it.toString(), style = CaducityTheme.typography.labelLarge)
            }
          }
        },
      )
    }
  }
}
