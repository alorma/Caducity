package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.CheckCircle
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.Edit
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * «Desats en aquesta tanda»: horizontal row of cards, newest first, running to the right edge so a piece of the
 * next card hints that it scrolls. A new entry scrolls the row back to the start.
 */
@Composable
fun BatchSavedRow(
  entries: List<AddedEntry>,
  categories: List<Category>,
  onEdit: (AddedEntry) -> Unit,
  onRemove: (AddedEntry) -> Unit,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  val newest = entries.firstOrNull()
  LaunchedEffect(newest) { listState.animateScrollToItem(0) }
  val listDescription = stringResource(R.string.add_batch_list_description)

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AddOverline(stringResource(R.string.add_batch_label))
    LazyRow(
      state = listState,
      modifier = Modifier.fullBleed(ScreenGutter).semantics { contentDescription = listDescription },
      contentPadding = PaddingValues(start = ScreenGutter),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(entries, key = { it.batch.itemIds.first() }) { entry ->
        BatchSavedCard(
          entry = entry,
          categoryName = categories.firstOrNull { it.id == entry.categoryId }?.name,
          onEdit = { onEdit(entry) },
          onRemove = { onRemove(entry) },
          modifier = Modifier.animateItem(),
        )
      }
    }
  }
}

@Composable
fun BatchSavedCard(
  entry: AddedEntry,
  categoryName: String?,
  onEdit: () -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // The whole card edits; the two icon buttons keep their own targets (and the accessible labels).
  Surface(
    onClick = onEdit,
    shape = RoundedCornerShape(20.dp),
    color = CaducityTheme.colorScheme.surfaceContainer,
    modifier = modifier.width(148.dp).height(148.dp),
  ) {
    Column(modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 6.dp, bottom = 6.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
          imageVector = Symbols.Filled.CheckCircle,
          contentDescription = null,
          tint = CaducityTheme.colors.fresh.strong,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = entry.name,
          style = CaducityTheme.typography.titleSmall.copy(fontSize = 15.sp),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
      if (categoryName != null) {
        Text(
          text = categoryName,
          style = CaducityTheme.typography.bodySmall.copy(fontSize = 12.sp),
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text =
          pluralStringResource(
            R.plurals.add_batch_units_date,
            entry.quantity,
            entry.quantity,
            entry.date.shortDayMonth(),
          ),
        style = CaducityTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
      )
      Row(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
      ) {
        IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
          Icon(
            imageVector = Symbols.Outlined.Edit,
            contentDescription = stringResource(R.string.add_batch_edit, entry.name),
            tint = CaducityTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(44.dp)) {
          Icon(
            imageVector = Symbols.Outlined.Delete,
            contentDescription = stringResource(R.string.add_batch_remove, entry.name),
            tint = CaducityTheme.colors.expired.strong,
            modifier = Modifier.size(20.dp),
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
