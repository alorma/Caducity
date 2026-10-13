package com.alorma.caducity.ui.screen.transform

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.ExpandLess
import com.alorma.caducity.base.ui.icons.symbols.ExpandMore
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.Widgets
import com.alorma.caducity.domain.usecase.TransformItemsUseCase
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.screen.add.NameField
import com.alorma.caducity.ui.screen.add.StepButton
import com.alorma.caducity.ui.screen.add.SuggestionRow
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

private val ChipShape = RoundedCornerShape(22.dp)

/**
 * The form of the selected result: name, servings, category, date and how many go to the freezer. It is the content of
 * the expanded card in the row, so the card (surface, border, size) belongs to the caller.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultForm(
  state: TransformState,
  result: ResultUi,
  actions: ResultActions,
  modifier: Modifier = Modifier,
) {
  val id = result.id
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  // Only a result that was just added (or the first one of the free entry) takes the focus; an existing one does not.
  LaunchedEffect(Unit) { if (result.name.isEmpty() && state.focusResultId == id) focusRequester.requestFocus() }
  var pickingCategory by rememberSaveable(id) { mutableStateOf(false) }
  val category = state.categories.firstOrNull { it.id == state.categoryOf(result) }
  val suggestion = result.suggestion

  Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      NameField(
        name = result.name,
        onNameChange = { actions.onNameChange(id, it) },
        onClear = { actions.onNameChange(id, "") },
        focusRequester = focusRequester,
        hint = stringResource(R.string.transform_result_name_hint),
      )
      if (suggestion != null) {
        SuggestionRow(
          suggestion = suggestion.name,
          typed = result.name,
          categoryName = state.categories.firstOrNull { it.id == suggestion.categoryId }?.name,
          onClick = { actions.onSuggestionClick(id) },
        )
      }
    }
    CounterRow(
      label = stringResource(R.string.transform_servings),
      value = result.servings,
      range = 1..TransformItemsUseCase.MAX_SERVINGS,
      onChange = { actions.onServingsChange(id, it) },
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      if (category != null) {
        ResultChip(onClick = { pickingCategory = !pickingCategory }) {
          CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 32.dp, selected = true)
          Text(category.name, style = CaducityTheme.typography.labelLarge, maxLines = 1)
          Icon(
            if (pickingCategory) Symbols.Outlined.ExpandLess else Symbols.Outlined.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
        }
      } else {
        ResultChip(onClick = { pickingCategory = !pickingCategory }, empty = true) {
          Icon(Symbols.Outlined.Widgets, contentDescription = null, modifier = Modifier.size(20.dp))
          Text(
            stringResource(R.string.transform_category_empty),
            style = CaducityTheme.typography.labelLarge,
            maxLines = 1,
          )
        }
      }
      val expiration = state.expirationOf(result)
      // The explanation of where the date comes from is no longer on screen: it is only the chip's description.
      val first = state.firstToExpire
      val dateHint =
        when {
          result.expirationOverride == null && first != null ->
            stringResource(R.string.transform_date_hint, first.name)
          state.ingredients.isEmpty() -> stringResource(R.string.transform_free_hint)
          else -> null
        }
      ResultChip(onClick = { actions.onPickDate(id) }, empty = expiration == null, description = dateHint) {
        Icon(Symbols.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(
          text =
            if (expiration != null) {
              formatDate(expiration, LocalLocale.current.platformLocale)
            } else {
              stringResource(R.string.transform_date_empty)
            },
          style = CaducityTheme.typography.labelLarge,
          maxLines = 1,
        )
      }
    }
    if (pickingCategory) {
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.categories.forEach { option ->
          val selected = option.id == category?.id
          ResultChip(
            onClick = {
              actions.onCategorySelect(id, option.id)
              pickingCategory = false
            },
            selected = selected,
          ) {
            CategoryShapeIcon(shape = option.shape, icon = option.icon.imageVector, size = 32.dp, selected = selected)
            Text(option.name, style = CaducityTheme.typography.labelLarge, maxLines = 1)
          }
        }
      }
    }
    FreezeSection(
      frozenCount = result.frozenCount,
      servings = result.servings,
      onChange = { actions.onFrozenCountChange(id, it) },
    )
    DoneButton(
      onClick = {
        focusManager.clearFocus()
        actions.onDone()
      },
      modifier = Modifier.align(Alignment.End),
    )
  }
}

/** «Fet»: folds the form back into its small card. It validates nothing. */
@Composable
private fun DoneButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    shape = CaducityShapes.full,
    color = CaducityTheme.colorScheme.primaryContainer,
    contentColor = CaducityTheme.colorScheme.onPrimaryContainer,
    modifier = modifier.minimumInteractiveComponentSize().height(40.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Symbols.Outlined.Check, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(stringResource(R.string.transform_result_done), style = CaducityTheme.typography.labelLarge)
    }
  }
}

/**
 * «Congela'n»: a dashed chip («Congela'n · N» once there is something to freeze) that unfolds into a frozen-coloured
 * row with the stepper (0..servings). Tapping the label folds it back.
 */
@Composable
private fun FreezeSection(
  frozenCount: Int,
  servings: Int,
  onChange: (Int) -> Unit,
) {
  var open by rememberSaveable { mutableStateOf(frozenCount > 0) }
  if (!open) {
    ResultChip(onClick = { open = true }, empty = frozenCount == 0, selected = false) {
      Icon(Symbols.Outlined.AcUnit, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(
        text =
          if (frozenCount > 0) {
            stringResource(R.string.transform_freeze_chip, frozenCount)
          } else {
            stringResource(R.string.transform_freeze)
          },
        style = CaducityTheme.typography.labelLarge,
        maxLines = 1,
      )
    }
    return
  }
  val frozen = CaducityTheme.colors.frozen
  Surface(
    shape = CaducityTheme.shapes.large,
    color = frozen.container,
    contentColor = frozen.onContainer,
    modifier = Modifier.fillMaxWidth().height(64.dp),
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.weight(1f).clickable { open = false },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(Symbols.Outlined.AcUnit, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(
          text = stringResource(R.string.transform_freeze_in_freezer, frozenCount),
          style = CaducityTheme.typography.labelLarge,
          maxLines = 2,
        )
      }
      Stepper(value = frozenCount, range = 0..servings, onChange = onChange)
    }
  }
}

/** `label  − 4 +` on a tonal bar; the value is announced politely. */
@Composable
private fun CounterRow(
  label: String,
  value: Int,
  range: IntRange,
  onChange: (Int) -> Unit,
  icon: ImageVector? = null,
) {
  Surface(
    shape = CaducityTheme.shapes.large,
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    modifier = Modifier.fillMaxWidth().height(64.dp),
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(text = label, style = CaducityTheme.typography.labelLarge, modifier = Modifier.weight(1f))
      Stepper(value = value, range = range, onChange = onChange)
    }
  }
}

/** `− 4 +`; the value is announced politely. */
@Composable
private fun Stepper(
  value: Int,
  range: IntRange,
  onChange: (Int) -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    StepButton(
      enabled = value > range.first,
      onClick = { onChange(value - 1) },
      label = stringResource(R.string.add_quantity_decrease),
    ) {
      Text("−", style = CaducityTheme.typography.titleMedium)
    }
    Box(modifier = Modifier.width(40.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
      Text(
        text = value.toString(),
        style = CaducityTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    StepButton(
      enabled = value < range.last,
      onClick = { onChange(value + 1) },
      label = stringResource(R.string.add_quantity_increase),
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
    }
  }
}

@Composable
private fun ResultChip(
  onClick: () -> Unit,
  selected: Boolean = false,
  /** A value still to be chosen: transparent, dashed outline. */
  empty: Boolean = false,
  /** Read by accessibility services instead of the chip's text, for what the screen no longer spells out. */
  description: String? = null,
  content: @Composable () -> Unit,
) {
  val scheme = CaducityTheme.colorScheme
  val dash = scheme.outline
  Surface(
    onClick = onClick,
    shape = ChipShape,
    color =
      when {
        empty -> Color.Transparent
        selected -> scheme.primaryContainer
        else -> scheme.surfaceContainerHigh
      },
    contentColor =
      when {
        empty -> scheme.onSurfaceVariant
        selected -> scheme.onPrimaryContainer
        else -> scheme.onSurface
      },
    border = if (selected) BorderStroke(2.dp, scheme.primary) else null,
    modifier =
      Modifier
        .minimumInteractiveComponentSize()
        .height(44.dp)
        .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
        .then(
          if (empty) {
            Modifier.drawBehind {
              drawRoundRect(
                color = dash,
                cornerRadius = CornerRadius(22.dp.toPx()),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))),
              )
            }
          } else {
            Modifier
          },
        ),
  ) {
    Row(
      modifier = Modifier.padding(start = 6.dp, end = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      content()
    }
  }
}
