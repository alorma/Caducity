package com.alorma.caducity.ui.screen.transform

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
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
import com.alorma.caducity.ui.screen.add.AddOverline
import com.alorma.caducity.ui.screen.add.NameField
import com.alorma.caducity.ui.screen.add.StepButton
import com.alorma.caducity.ui.screen.add.SuggestionRow
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

private val CardShape = RoundedCornerShape(24.dp)
private val ChipShape = RoundedCornerShape(22.dp)

/** One product the transformation creates: name, servings, category, date and how many go to the freezer. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultCard(
  state: TransformState,
  result: ResultUi,
  actions: ResultActions,
  modifier: Modifier = Modifier,
) {
  val id = result.id
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) { if (result.name.isEmpty()) focusRequester.requestFocus() }
  var pickingCategory by rememberSaveable(id) { mutableStateOf(false) }
  val category = state.categories.firstOrNull { it.id == state.categoryOf(result) }
  val suggestion = result.suggestion

  Surface(shape = CardShape, color = CaducityTheme.colorScheme.surfaceContainer, modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AddOverline(stringResource(R.string.transform_result_name))
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
        ResultChip(onClick = { actions.onPickDate(id) }, empty = expiration == null) {
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
      val first = state.firstToExpire
      if (result.expirationOverride == null && first != null) {
        Text(
          text = stringResource(R.string.transform_date_hint, first.name),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      } else if (state.ingredients.isEmpty() && (category == null || state.expirationOf(result) == null)) {
        Text(
          text = stringResource(R.string.transform_free_hint),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
      FreezeRow(
        frozenCount = result.frozenCount,
        servings = result.servings,
        onChange = { actions.onFrozenCountChange(id, it) },
      )
      DoneButton(onClick = actions.onDone, modifier = Modifier.align(Alignment.End))
    }
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

@Composable
private fun FreezeRow(
  frozenCount: Int,
  servings: Int,
  onChange: (Int) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    CounterRow(
      label = stringResource(R.string.transform_freeze),
      value = frozenCount,
      range = 0..servings,
      onChange = onChange,
      icon = Symbols.Outlined.AcUnit,
    )
    Text(
      text = stringResource(R.string.transform_freeze_summary, servings - frozenCount, frozenCount),
      style = CaducityTheme.typography.bodySmall,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 4.dp),
    )
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
}

@Composable
private fun ResultChip(
  onClick: () -> Unit,
  selected: Boolean = false,
  /** A value still to be chosen: transparent, dashed outline. */
  empty: Boolean = false,
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
      Modifier.minimumInteractiveComponentSize().height(44.dp).then(
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
