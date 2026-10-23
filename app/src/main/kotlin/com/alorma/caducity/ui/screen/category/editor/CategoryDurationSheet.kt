package com.alorma.caducity.ui.screen.category.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Lightbulb
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.ThresholdStops
import com.alorma.caducity.ui.components.category.FormOverline
import com.alorma.caducity.ui.components.threshold.thresholdLabel
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

private val DAY_OPTIONS = ThresholdStops.days.filter { it < 7 }
private val WEEK_AND_MONTH_OPTIONS = ThresholdStops.days.filter { it >= 7 }

/** «Durada habitual» sheet: the old slider's stops as buttons, plus a sentence that explains the picked value. */
@Composable
internal fun CategoryDurationSheet(
  categoryName: String,
  usualDurationDays: Int?,
  onDurationChange: (Int?) -> Unit,
  onDone: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val selected = ThresholdStops.selectedStop(usualDurationDays)
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Text(
      text = stringResource(R.string.category_editor_duration_row),
      style = CaducityTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    )
    DurationGroup(title = stringResource(R.string.category_editor_duration_days)) {
      DAY_OPTIONS.forEach { days ->
        DurationOption(thresholdLabel(days), selected == days) { onDurationChange(days) }
      }
    }
    DurationGroup(title = stringResource(R.string.category_editor_duration_weeks_months)) {
      WEEK_AND_MONTH_OPTIONS.forEach { days ->
        DurationOption(thresholdLabel(days), selected == days) { onDurationChange(days) }
      }
      DurationOption(stringResource(R.string.category_editor_duration_none), usualDurationDays == null) {
        onDurationChange(null)
      }
    }
    DurationHelp(categoryName = categoryName.trim(), days = selected)
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(R.string.category_editor_done))
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DurationGroup(
  title: String,
  content: @Composable () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    FormOverline(title)
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      content()
    }
  }
}

@Composable
private fun DurationOption(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val scheme = CaducityTheme.colorScheme
  Surface(
    selected = selected,
    onClick = onClick,
    shape = CaducityShapes.field,
    color = if (selected) scheme.primary else scheme.surfaceContainerHigh,
    contentColor = if (selected) scheme.onPrimary else scheme.onSurface,
    border = if (selected) null else BorderStroke(1.dp, scheme.outlineVariant),
  ) {
    Row(
      modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (selected) Icon(Symbols.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
      Text(text = label, style = CaducityTheme.typography.labelLarge)
    }
  }
}

/** The 💡 box: what the picked value means when a product is added. */
@Composable
private fun DurationHelp(
  categoryName: String,
  days: Int?,
) {
  Surface(shape = CaducityShapes.field, color = CaducityTheme.colorScheme.surfaceContainer) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        Symbols.Outlined.Lightbulb,
        contentDescription = null,
        tint = CaducityTheme.colorScheme.primary,
      )
      Text(
        text = durationHelpText(categoryName, days),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun durationHelpText(
  categoryName: String,
  days: Int?,
): String =
  when {
    days == null -> stringResource(R.string.category_editor_duration_sheet_none)
    categoryName.isBlank() ->
      stringResource(R.string.category_editor_duration_sheet_blank, thresholdLabel(days))
    else -> stringResource(R.string.category_editor_duration_sheet_help, categoryName, thresholdLabel(days))
  }
