package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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
    .RoundedCornerShape(22.dp)

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
      Surface(
        shape = ChipShape,
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainer,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        border = if (selected) BorderStroke(2.dp, CaducityTheme.colorScheme.primary) else null,
        modifier =
          Modifier
            .height(44.dp)
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton) { onSelect(category.id) },
      ) {
        Row(
          modifier = Modifier.padding(start = 5.dp, end = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 34.dp, selected = selected)
          Text(category.name, style = CaducityTheme.typography.labelLarge, maxLines = 1)
        }
      }
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
  Surface(
    shape = ChipShape,
    color = if (highlight) scheme.primaryContainer else scheme.surface,
    contentColor = if (highlight) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
    modifier =
      Modifier
        .height(44.dp)
        .drawBehind {
          drawRoundRect(
            color = outline,
            cornerRadius = CornerRadius(22.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
          )
        }.clickable(role = Role.Button, onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(stringResource(R.string.add_category_new), style = CaducityTheme.typography.labelLarge)
    }
  }
}
