package com.alorma.caducity.ui.components.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.ExpandLess
import com.alorma.caducity.base.ui.icons.symbols.ExpandMore
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.ProductGroup
import com.alorma.caducity.ui.components.shape.connectedShape
import com.alorma.caducity.ui.components.status.StatusPill
import com.alorma.caducity.ui.components.status.statusPillText
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaLocalDate

/** «dg. 11 oct». */
internal fun LocalDate.shortDateText(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("EEE d MMM", locale).format(toJavaLocalDate())

/**
 * A product of a category as a row of a connected list. A single unit is a plain row; several units
 * show «N unitats · propera …» and expand into one row per unit («Unitat N»).
 *
 * @param categoryName shown in the meta line (search results); null inside a category.
 */
@Composable
fun ProductGroupRow(
  group: ProductGroup,
  today: LocalDate,
  index: Int,
  count: Int,
  expanded: Boolean,
  onToggleExpanded: () -> Unit,
  onUnitClick: (itemId: String) -> Unit,
  modifier: Modifier = Modifier,
  categoryName: String? = null,
) {
  val nearest = group.nearest
  val nearestDate = nearest.expiration.shortDateText()
  val quantity =
    if (group.isSingle) {
      null
    } else {
      pluralStringResource(R.plurals.home_unit_count, group.unitCount, group.unitCount)
    }
  val meta =
    listOfNotNull(
      categoryName,
      quantity,
      if (group.isSingle) nearestDate else stringResource(R.string.category_next, nearestDate),
    ).joinToString(" · ")
  val pill = statusPillText(nearest.status, today.daysUntil(nearest.expiration))

  Surface(
    modifier = modifier,
    shape = connectedShape(index, count),
    color = CaducityTheme.colorScheme.surfaceContainer,
    contentColor = CaducityTheme.colorScheme.onSurface,
  ) {
    Column {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button) {
              if (group.isSingle) onUnitClick(nearest.itemId) else onToggleExpanded()
            }.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(group.name, style = CaducityTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
          Text(
            meta,
            style = CaducityTheme.typography.bodySmall,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Spacer(Modifier.width(8.dp))
        StatusPill(status = nearest.status, label = pill)
        if (!group.isSingle) {
          Icon(
            imageVector = if (expanded) Symbols.Outlined.ExpandLess else Symbols.Outlined.ExpandMore,
            contentDescription = null,
            modifier = Modifier.padding(start = 4.dp).size(24.dp),
          )
        }
      }
      AnimatedVisibility(visible = expanded && !group.isSingle) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
          group.units.forEachIndexed { position, unit -> UnitRow(unit, position + 1, onUnitClick) }
        }
      }
    }
  }
}

@Composable
private fun UnitRow(
  unit: PantryEntry,
  number: Int,
  onClick: (String) -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .heightIn(min = 52.dp)
        .clickable(role = Role.Button) { onClick(unit.itemId) }
        .padding(start = 32.dp, end = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(
      Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(unit.status.colors().strong),
    )
    Spacer(Modifier.width(12.dp))
    Text(
      text = stringResource(R.string.category_unit_n, number),
      style = CaducityTheme.typography.bodyMedium,
      modifier = Modifier.weight(1f),
    )
    Text(
      text = unit.expiration.shortDateText(),
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
  }
}
