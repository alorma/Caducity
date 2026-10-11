package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** «En aquesta tanda»: one chip per saved product, «Nom ×N · dd/mm». */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchEntries(
  entries: List<AddedEntry>,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AddOverline(stringResource(R.string.add_batch_label))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      entries.forEach { entry ->
        Surface(
          shape = CaducityShapes.chip,
          color = CaducityTheme.colorScheme.surfaceContainer,
          contentColor = CaducityTheme.colorScheme.onSurface,
        ) {
          val quantity = if (entry.quantity > 1) " ×${entry.quantity}" else ""
          Text(
            text = "${entry.name}$quantity · ${entry.date.shortDayMonth()}",
            style = CaducityTheme.typography.labelLarge,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }
    }
  }
}

/** In batch mode category and date are summary rows with «Canvia», plus the note about keeping them. */
@Composable
fun BatchSummary(
  category: Category?,
  date: LocalDate?,
  today: LocalDate,
  onChangeCategory: () -> Unit,
  onChangeDate: () -> Unit,
  showCategory: Boolean,
  showDate: Boolean,
  modifier: Modifier = Modifier,
) {
  val rows = listOf(showCategory, showDate).count { it }
  if (rows == 0) return
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Column(verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
      var index = 0
      if (showCategory) {
        ConnectedRow(index = index++, count = rows, onClick = onChangeCategory) {
          if (category != null) {
            CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 40.dp, selected = true)
          }
          SummaryText(
            label = stringResource(R.string.add_category_label),
            value = category?.name ?: "—",
            modifier = Modifier.weight(1f).padding(start = 16.dp),
          )
          ChangeLabel()
        }
      }
      if (showDate) {
        ConnectedRow(index = index, count = rows, onClick = onChangeDate) {
          Icon(
            Symbols.Outlined.CalendarMonth,
            contentDescription = null,
            tint = CaducityTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp).padding(8.dp),
          )
          SummaryText(
            label = stringResource(R.string.add_date_label),
            value = date?.let { "${it.shortDate()} · ${relativeDays(it, today)}" } ?: "—",
            modifier = Modifier.weight(1f).padding(start = 16.dp),
          )
          ChangeLabel()
        }
      }
    }
    Text(
      text = stringResource(R.string.add_batch_note),
      style = CaducityTheme.typography.bodySmall,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 4.dp),
    )
  }
}

@Composable
private fun SummaryText(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Text(label, style = CaducityTheme.typography.bodySmall, color = CaducityTheme.colorScheme.onSurfaceVariant)
    Text(value, style = CaducityTheme.typography.titleSmall, maxLines = 1)
  }
}

@Composable
private fun ChangeLabel() {
  Text(
    text = stringResource(R.string.add_batch_change),
    style = CaducityTheme.typography.labelLarge,
    color = CaducityTheme.colorScheme.primary,
    modifier = Modifier.padding(start = 8.dp),
  )
}

@Composable
private fun relativeDays(
  date: LocalDate,
  today: LocalDate,
): String {
  val days = today.daysUntil(date)
  return when {
    days < 0 -> stringResource(R.string.add_date_help_expired)
    days == 0 -> stringResource(R.string.add_batch_today)
    days == 1 -> stringResource(R.string.add_batch_tomorrow)
    else -> pluralStringResource(R.plurals.add_batch_in_days, days, days)
  }
}

private fun LocalDate.shortDayMonth() = "%02d/%02d".format(dayOfMonth, monthNumber)

private fun LocalDate.shortDate() = "%02d/%02d/%02d".format(dayOfMonth, monthNumber, year % 100)
