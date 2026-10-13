package com.alorma.caducity.ui.screen.transform

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.screen.add.ScreenGutter
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

/** What the results section can ask of the screen, one callback per gesture. */
class ResultActions(
  val onSelect: (Int) -> Unit,
  val onDone: () -> Unit,
  val onRemove: (Int) -> Unit,
  val onAdd: () -> Unit,
  val onNameChange: (Int, String) -> Unit,
  val onSuggestionClick: (Int) -> Unit,
  val onServingsChange: (Int, Int) -> Unit,
  val onCategorySelect: (Int, String) -> Unit,
  val onPickDate: (Int) -> Unit,
  val onFrozenCountChange: (Int, Int) -> Unit,
)

private val ResultCardHeight = 92.dp

/**
 * «Resultat · M»: a horizontal row of small cards, one per product. The selected one is the form, shown below the
 * row. With a single result the row is hidden and only the form shows; «Fet» folds it back into its card.
 */
@Composable
fun ResultsSection(
  state: TransformState,
  actions: ResultActions,
  modifier: Modifier = Modifier,
) {
  val expanded = state.results.firstOrNull { it.id == state.expandedResultId }
  val showRow = state.results.size > 1 || expanded == null
  val listState = rememberLazyListState()
  LaunchedEffect(state.expandedResultId, showRow) {
    val index = state.results.indexOfFirst { it.id == state.expandedResultId }
    if (showRow && index >= 0) listState.animateScrollToItem(index)
  }
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    RowSectionHeader(
      title = stringResource(R.string.transform_section_results_count, state.results.size),
      onAdd = actions.onAdd,
    )
    if (showRow) {
      LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = ScreenGutter),
        horizontalArrangement = Arrangement.spacedBy(RowCardSpacing),
        modifier =
          Modifier.semantics {
            collectionInfo =
              CollectionInfo(
                rowCount = 1,
                columnCount = state.results.size,
              )
          },
      ) {
        items(state.results, key = { it.id }) { result ->
          ResultSummaryCard(
            state = state,
            result = result,
            selected = result.id == state.expandedResultId,
            onClick = { actions.onSelect(result.id) },
          )
        }
      }
    }
    if (expanded != null) {
      key(expanded.id) {
        ResultCard(
          state = state,
          result = expanded,
          actions = actions,
          modifier = Modifier.padding(horizontal = ScreenGutter),
        )
      }
    }
  }
}

/** A folded result: shape, name, «12 units · Category», the frozen pill and a dot when it still lacks something. */
@Composable
private fun ResultSummaryCard(
  state: TransformState,
  result: ResultUi,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val category = state.categories.firstOrNull { it.id == state.categoryOf(result) }
  val name = result.name.ifBlank { stringResource(R.string.transform_result_unnamed) }
  Surface(
    onClick = onClick,
    shape = RowCardShape,
    color = CaducityTheme.colorScheme.surfaceContainer,
    border = if (selected) BorderStroke(2.dp, CaducityTheme.colorScheme.primary) else null,
    modifier = Modifier.size(width = RowCardWidth, height = ResultCardHeight),
  ) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (category != null) {
          CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 28.dp)
        }
        Text(
          text = name,
          style = CaducityTheme.typography.titleSmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        if (!state.isValid(result)) InvalidDot()
      }
      Text(
        text = resultMeta(result, category?.name),
        style = CaducityTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (result.frozenCount > 0) FrozenChip(result.frozenCount)
    }
  }
}

@Composable
private fun InvalidDot() {
  val label = stringResource(R.string.transform_result_invalid)
  Box(
    modifier =
      Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(CaducityTheme.colorScheme.error)
        .semantics { contentDescription = label },
  )
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
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
    )
  }
}

@Composable
private fun resultMeta(
  result: ResultUi,
  categoryName: String?,
): String {
  val units = pluralStringResource(R.plurals.transform_result_units, result.servings, result.servings)
  return listOfNotNull(units, categoryName).joinToString(" · ")
}
