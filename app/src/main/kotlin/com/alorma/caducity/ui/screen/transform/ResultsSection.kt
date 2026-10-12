package com.alorma.caducity.ui.screen.transform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.ExpandLess
import com.alorma.caducity.base.ui.icons.symbols.ExpandMore
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

/** What the results section can ask of the screen, one callback per gesture. */
class ResultActions(
  val onToggle: (Int) -> Unit,
  val onRemove: (Int) -> Unit,
  val onAdd: () -> Unit,
  val onNameChange: (Int, String) -> Unit,
  val onSuggestionClick: (Int) -> Unit,
  val onServingsChange: (Int, Int) -> Unit,
  val onCategorySelect: (Int, String) -> Unit,
  val onPickDate: (Int) -> Unit,
  val onFrozenCountChange: (Int, Int) -> Unit,
)

/**
 * «Resultat»: a single result is always open; with several, each is a folded row (summary, chevron, ×) that
 * expands into the full card, one at a time.
 */
@Composable
fun ResultsSection(
  state: TransformState,
  actions: ResultActions,
  modifier: Modifier = Modifier,
) {
  val several = state.results.size > 1
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text =
        if (several) {
          stringResource(R.string.transform_section_results, state.results.size)
        } else {
          stringResource(R.string.transform_section_result)
        },
      style = CaducityTheme.typography.labelLarge,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 4.dp),
    )
    state.results.forEach { result ->
      key(result.id) {
        if (several) {
          FoldedResult(
            state = state,
            result = result,
            expanded = state.expandedResultId == result.id,
            actions = actions,
          )
        } else {
          ResultCard(state = state, result = result, actions = actions)
        }
      }
    }
    AddResultRow(onClick = actions.onAdd)
    if (several) {
      Text(
        text = stringResource(R.string.transform_results_hint),
        style = CaducityTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
      )
    }
  }
}

@Composable
private fun FoldedResult(
  state: TransformState,
  result: ResultUi,
  expanded: Boolean,
  actions: ResultActions,
) {
  val category = state.categories.firstOrNull { it.id == state.categoryOf(result) }
  val name = result.name.ifBlank { stringResource(R.string.transform_result_unnamed) }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ConnectedRow(
      index = 0,
      count = 1,
      onClick = { actions.onToggle(result.id) },
      contentPadding = PaddingValues(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
    ) {
      if (category != null) {
        CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 44.dp)
      }
      Column(
        modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(text = name, style = CaducityTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
          text = resultMeta(state, result, category?.name),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
      if (result.frozenCount > 0) FrozenChip(result.frozenCount)
      Icon(
        if (expanded) Symbols.Outlined.ExpandLess else Symbols.Outlined.ExpandMore,
        contentDescription = null,
        modifier = Modifier.padding(start = 4.dp).size(24.dp),
      )
      IconButton(onClick = { actions.onRemove(result.id) }) {
        Icon(
          Symbols.Outlined.Close,
          contentDescription = stringResource(R.string.transform_result_remove, name),
          modifier = Modifier.size(20.dp),
        )
      }
    }
    AnimatedVisibility(
      visible = expanded,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut(),
    ) {
      ResultCard(state = state, result = result, actions = actions)
    }
  }
}

@Composable
private fun FrozenChip(count: Int) {
  val label = pluralStringResource(R.plurals.transform_result_frozen_chip, count, count)
  Surface(
    shape = CaducityShapes.full,
    color = CaducityTheme.colors.frozen.container,
    contentColor = CaducityTheme.colors.frozen.onContainer,
    modifier = Modifier.semantics { contentDescription = label },
  ) {
    Text(
      text = label,
      style = CaducityTheme.typography.labelMedium,
      maxLines = 1,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

@Composable
private fun AddResultRow(onClick: () -> Unit) {
  Surface(
    onClick = onClick,
    shape = CaducityShapes.card,
    color = CaducityTheme.colorScheme.secondaryContainer,
    contentColor = CaducityTheme.colorScheme.onSecondaryContainer,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(stringResource(R.string.transform_add_result), style = CaducityTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun resultMeta(
  state: TransformState,
  result: ResultUi,
  categoryName: String?,
): String {
  val units = pluralStringResource(R.plurals.transform_result_units, result.servings, result.servings)
  val date = formatDate(state.expirationOf(result), LocalLocale.current.platformLocale)
  return listOfNotNull(units, categoryName, stringResource(R.string.transform_result_expires, date)).joinToString(" · ")
}
