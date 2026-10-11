package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.components.chips.QuickDateChips
import com.alorma.caducity.ui.components.chips.QuickDateOption
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Typed expiration date (`dd/mm/aa`). The state is the raw [digits]; the slashes are inserted here.
 * [date] is the parsed value (null while incomplete) and drives the live help text.
 */
@Composable
fun ExpiryDateField(
  digits: String,
  date: LocalDate?,
  today: LocalDate,
  onDigitsChange: (String) -> Unit,
  onPickerClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val focused by interactionSource.collectIsFocusedAsState()
  val formatted = ExpiryDateParser.format(digits)
  val expired = date != null && date < today

  Surface(
    shape = CaducityShapes.field,
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    border = if (focused) BorderStroke(2.dp, CaducityTheme.colorScheme.primary) else null,
    modifier = modifier.fillMaxWidth().height(72.dp),
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
        val textStyle =
          CaducityTheme.typography.headlineMedium.copy(
            fontSize = 32.sp,
            color = CaducityTheme.colorScheme.onSurface,
          )
        if (formatted.isEmpty()) {
          Text(
            text = stringResource(R.string.add_date_placeholder),
            style = textStyle.copy(color = CaducityTheme.colorScheme.outline),
          )
        }
        BasicTextField(
          value = TextFieldValue(formatted, selection = TextRange(formatted.length)),
          onValueChange = { onDigitsChange(ExpiryDateParser.normalize(it.text)) },
          singleLine = true,
          textStyle = textStyle,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          cursorBrush =
            androidx.compose.ui.graphics
              .SolidColor(CaducityTheme.colorScheme.primary),
          interactionSource = interactionSource,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      DateHelp(date = date, today = today, expired = expired)
      IconButton(onClick = onPickerClick, modifier = Modifier.size(48.dp)) {
        Icon(
          imageVector = Symbols.Outlined.CalendarMonth,
          contentDescription = stringResource(R.string.add_date_pick),
          tint = CaducityTheme.colorScheme.primary,
        )
      }
    }
  }
}

@Composable
private fun DateHelp(
  date: LocalDate?,
  today: LocalDate,
  expired: Boolean,
) {
  val text =
    when {
      date == null -> null
      expired -> stringResource(R.string.add_date_help_expired)
      else -> {
        val days = today.daysUntil(date)
        val weekday = date.dayOfWeek.localizedName()
        when (days) {
          0 -> stringResource(R.string.add_date_help_today, weekday)
          1 -> stringResource(R.string.add_date_help_tomorrow, weekday)
          else -> pluralStringResource(R.plurals.add_date_help_days, days, weekday, days)
        }
      }
    }
  Column(
    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    horizontalAlignment = Alignment.End,
  ) {
    if (text != null) {
      Text(
        text = text,
        style = CaducityTheme.typography.labelMedium,
        textAlign = TextAlign.End,
        color = if (expired) CaducityTheme.colors.expired.strong else CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
      )
    }
  }
}

private fun DayOfWeek.localizedName(): String =
  java.time.DayOfWeek
    .of(ordinal + 1)
    .getDisplayName(TextStyle.FULL, Locale.getDefault())

/** Row of quick dates (tomorrow, +3 days, ...) that reports the chosen [QuickDate]. */
@Composable
fun QuickDateRow(
  onSelect: (QuickDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val options =
    QuickDate.entries.map { quick ->
      QuickDateOption(
        label =
          stringResource(
            when (quick) {
              QuickDate.Tomorrow -> R.string.add_quick_tomorrow
              QuickDate.ThreeDays -> R.string.add_quick_3_days
              QuickDate.OneWeek -> R.string.add_quick_1_week
              QuickDate.TwoWeeks -> R.string.add_quick_2_weeks
              QuickDate.OneMonth -> R.string.add_quick_1_month
            },
          ),
      )
    }
  QuickDateChips(
    options = options,
    onClick = { onSelect(QuickDate.entries[it]) },
    modifier = modifier,
  )
}

@Preview(name = "Expiry date field")
@Composable
private fun ExpiryDateFieldPreview() {
  PreviewTheme {
    Surface {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val today = LocalDate(2026, 10, 11)
        ExpiryDateField("", null, today, {}, {})
        ExpiryDateField("211026", LocalDate(2026, 10, 21), today, {}, {})
        ExpiryDateField("011026", LocalDate(2026, 10, 1), today, {}, {})
        QuickDateRow(onSelect = {})
      }
    }
  }
}
