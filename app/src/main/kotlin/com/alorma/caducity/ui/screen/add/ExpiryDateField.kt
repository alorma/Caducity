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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate

/**
 * Typed expiration date (`dd/mm/aa`). The state is the raw [digits]; the slashes are inserted here.
 * [date] is the parsed value (null while incomplete); a date before [today] says so.
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
    modifier = modifier.fillMaxWidth().height(76.dp),
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, end = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
        val textStyle =
          CaducityTheme.typography.bodyLarge.copy(
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            color = CaducityTheme.colorScheme.onSurface,
          )
        if (formatted.isEmpty()) {
          Text(
            text = stringResource(R.string.add_date_placeholder),
            style = textStyle.copy(color = CaducityTheme.colorScheme.onSurfaceVariant),
          )
        }
        BasicTextField(
          value = TextFieldValue(formatted, selection = TextRange(formatted.length)),
          onValueChange = { onDigitsChange(ExpiryDateParser.normalize(it.text)) },
          singleLine = true,
          textStyle = textStyle,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          cursorBrush = SolidColor(CaducityTheme.colorScheme.primary),
          interactionSource = interactionSource,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      ExpiredHelp(expired = expired)
      FilledIconButton(
        onClick = onPickerClick,
        colors =
          IconButtonDefaults.filledIconButtonColors(
            containerColor = CaducityTheme.colorScheme.surface,
            contentColor = CaducityTheme.colorScheme.primary,
          ),
        modifier = Modifier.size(52.dp),
      ) {
        Icon(
          imageVector = Symbols.Outlined.CalendarMonth,
          contentDescription = stringResource(R.string.add_date_pick),
        )
      }
    }
  }
}

@Composable
private fun ExpiredHelp(expired: Boolean) {
  Column(
    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    horizontalAlignment = Alignment.End,
  ) {
    if (expired) {
      Text(
        text = stringResource(R.string.add_date_help_expired),
        style = CaducityTheme.typography.labelMedium,
        textAlign = TextAlign.End,
        color = CaducityTheme.colors.expired.strong,
        modifier = Modifier.padding(horizontal = 4.dp),
      )
    }
  }
}

/** Shortcuts in a 3 × 2 grid; the one whose date equals [date] (relative to [today]) is marked. */
@Composable
fun QuickDateGrid(
  onSelect: (QuickDate) -> Unit,
  modifier: Modifier = Modifier,
  date: LocalDate? = null,
  today: LocalDate? = null,
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    QuickDate.entries.chunked(GRID_COLUMNS).forEach { rowItems ->
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        rowItems.forEach { quick ->
          val selected = date != null && today != null && quick.from(today) == date
          QuickDateChip(
            label = stringResource(quick.labelRes),
            selected = selected,
            onClick = { onSelect(quick) },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

private const val GRID_COLUMNS = 3

@Composable
private fun QuickDateChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scheme = CaducityTheme.colorScheme
  FilterChip(
    selected = selected,
    onClick = onClick,
    shape = CaducityShapes.field,
    modifier = modifier.height(48.dp),
    border =
      FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = selected,
        borderColor = scheme.outlineVariant,
        selectedBorderColor = scheme.primary,
        selectedBorderWidth = 2.dp,
      ),
    colors =
      FilterChipDefaults.filterChipColors(
        containerColor = scheme.surface,
        labelColor = scheme.onSurface,
        selectedContainerColor = scheme.primaryContainer,
        selectedLabelColor = scheme.onPrimaryContainer,
      ),
    label = {
      Text(
        text = label,
        style = CaducityTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
      )
    },
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
        QuickDateGrid(onSelect = {}, date = LocalDate(2026, 10, 18), today = today)
      }
    }
  }
}
