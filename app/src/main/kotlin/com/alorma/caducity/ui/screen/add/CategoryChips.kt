package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.theme.CaducityTheme

private val ChipShape =
  androidx.compose.foundation.shape
    .RoundedCornerShape(26.dp)

/** Categories in several lines (no scroll) plus the trailing dashed «+ Nova» chip. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
  categories: List<Category>,
  selectedId: String?,
  highlightNew: Boolean,
  onSelect: (String) -> Unit,
  onNew: () -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    val scheme = CaducityTheme.colorScheme
    categories.forEach { category ->
      val selected = category.id == selectedId
      FilterChip(
        selected = selected,
        onClick = { onSelect(category.id) },
        shape = ChipShape,
        modifier = Modifier.height(52.dp),
        border =
          FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.Transparent,
            selectedBorderColor = scheme.primary,
            selectedBorderWidth = 2.dp,
          ),
        colors =
          FilterChipDefaults.filterChipColors(
            containerColor = scheme.surfaceContainer,
            labelColor = scheme.onSurface,
            selectedContainerColor = scheme.primaryContainer,
            selectedLabelColor = scheme.onPrimaryContainer,
          ),
        leadingIcon = {
          CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 40.dp, selected = selected)
        },
        label = { Text(category.name, style = CaducityTheme.typography.labelLarge, maxLines = 1) },
      )
    }
    NewCategoryChip(highlight = highlightNew, onClick = onNew)
  }
}

@Composable
private fun NewCategoryChip(
  highlight: Boolean,
  onClick: () -> Unit,
) {
  val scheme = CaducityTheme.colorScheme
  val outline = scheme.outline
  // The dashed outline has no native equivalent: AssistChip only takes a solid BorderStroke.
  AssistChip(
    onClick = onClick,
    shape = ChipShape,
    colors =
      AssistChipDefaults.assistChipColors(
        containerColor = if (highlight) scheme.primaryContainer else scheme.surface,
        labelColor = if (highlight) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        leadingIconContentColor = if (highlight) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
      ),
    border = null,
    modifier =
      Modifier
        .height(52.dp)
        .drawBehind {
          drawRoundRect(
            color = outline,
            cornerRadius = CornerRadius(26.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
          )
        },
    leadingIcon = { Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
    label = { Text(stringResource(R.string.add_category_new), style = CaducityTheme.typography.labelLarge) },
  )
}
