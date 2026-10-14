package com.alorma.caducity.ui.screen.transform

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.screen.add.ScreenGutter
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.coroutines.delay

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

/** Air around the lines that join the rows: between the ingredients and the node, and between the node and the results. */
internal val SectionSpacing = 52.dp
private const val ExpandMillis = 300

/** M3 emphasized decelerate: what the expansion of a card uses. */
private val EmphasizedEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/**
 * «Resultat · M»: a horizontal row of small cards, one per product. The selected one expands in place, inside the
 * same row, into the form (the screen width minus the gutters, so the neighbours peek at the edges); «Fet» folds it
 * back, and then every result is a small card.
 */
@Composable
fun ResultsSection(
  state: TransformState,
  actions: ResultActions,
  modifier: Modifier = Modifier,
  /** Where the row is, for the lines that join the ingredients to it. */
  links: RowLinks? = null,
) {
  // Starts with the selected card in place, so the form is already centred the first time it shows.
  val listState =
    rememberLazyListState(
      initialFirstVisibleItemIndex = state.results.indexOfFirst { it.id == state.expandedResultId }.coerceAtLeast(0),
    )
  // What the row shows follows the selection with one step in between: a folded card is first brought to the centre
  // and only then expands. Folding follows at once.
  var shownId by remember { mutableIntStateOf(state.expandedResultId) }
  LaunchedEffect(state.expandedResultId, state.results.size) {
    val target = state.expandedResultId
    if (target == NONE_EXPANDED) {
      shownId = NONE_EXPANDED
      return@LaunchedEffect
    }
    val index = state.results.indexOfFirst { it.id == target }
    if (index < 0) return@LaunchedEffect
    if (shownId == NONE_EXPANDED) listState.animateScrollToItem(index)
    shownId = target
    listState.animateScrollToItem(index)
    // The cards around it are still changing size while the first scroll runs: align once they have settled.
    delay(ExpandMillis.toLong())
    listState.animateScrollToItem(index)
  }
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SectionSpacing)) {
    // A lone result of the free entry is just «Resultat»: «Resultat · 1» says nothing.
    RowSectionHeader(
      title =
        if (state.freeEntry && state.results.size == 1) {
          stringResource(R.string.transform_section_result)
        } else {
          stringResource(R.string.transform_section_results_count, state.results.size)
        },
      onAdd = if (state.canAddResult) actions.onAdd else null,
      center = { RowLinksNode(links) },
    )
    SideEffect {
      links?.resultsState = listState
    }
    BoxWithConstraints {
      val formWidth = maxWidth - ScreenGutter * 2
      // With a form open it fills the row between the gutters. Folded, the padding on both sides is what puts one
      // card in the middle, so the first and the last can be scrolled to the centre (and a lone result starts there).
      val sidePadding by animateDpAsState(
        targetValue =
          if (shownId == NONE_EXPANDED) {
            ((maxWidth - RowCardWidth) / 2).coerceAtLeast(ScreenGutter)
          } else {
            ScreenGutter
          },
        animationSpec = tween(ExpandMillis, easing = EmphasizedEasing),
        label = "resultsSidePadding",
      )
      LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = sidePadding),
        horizontalArrangement = Arrangement.spacedBy(RowCardSpacing),
        modifier =
          Modifier
            .onGloballyPositioned { links?.resultsRow = it }
            .semantics {
              collectionInfo = CollectionInfo(rowCount = 1, columnCount = state.results.size)
            },
      ) {
        itemsIndexed(state.results, key = { _, result -> result.id }) { index, result ->
          ResultItem(
            state = state,
            result = result,
            index = index,
            listState = listState,
            selected = result.id == shownId,
            formWidth = formWidth,
            actions = actions,
          )
        }
      }
    }
  }
}

/** One result in the row: a small card, or the form when it is the selected one. */
@Composable
private fun ResultItem(
  state: TransformState,
  result: ResultUi,
  index: Int,
  listState: LazyListState,
  selected: Boolean,
  formWidth: Dp,
  actions: ResultActions,
) {
  // The thin edge of a neighbour is too small to aim at (and gestures are not the only way): screen readers and
  // switch access move between results with these two actions.
  val previous = state.results.getOrNull(index - 1)
  val next = state.results.getOrNull(index + 1)
  val previousLabel = stringResource(R.string.transform_result_previous)
  val nextLabel = stringResource(R.string.transform_result_next)
  // A folded card has the border of its line, with the same opacity: the further from the middle, the fainter.
  val lineAlpha by resultLineAlpha(listState, index)
  Card(
    onClick = { actions.onSelect(result.id) },
    enabled = !selected,
    shape = RowCardShape,
    colors =
      CardDefaults.cardColors(
        containerColor = CaducityTheme.colorScheme.surfaceContainer,
        disabledContainerColor = CaducityTheme.colorScheme.surfaceContainer,
      ),
    border =
      BorderStroke(
        2.dp,
        if (selected) CaducityTheme.colorScheme.primary else CaducityTheme.colorScheme.outline.copy(alpha = lineAlpha),
      ),
    modifier =
      Modifier
        // Width and height are animated together by the one modifier, so the card never turns tall and narrow.
        .animateContentSize(tween(ExpandMillis, easing = EmphasizedEasing))
        .width(if (selected) formWidth else RowCardWidth)
        // Folded, the card is its final height from the first frame (the form is still fading out inside it), so the
        // size animation goes from the form to the small card in one go instead of waiting for the form to leave.
        .then(if (selected) Modifier else Modifier.height(ResultCardHeight))
        .semantics {
          this.selected = selected
          collectionItemInfo = CollectionItemInfo(rowIndex = 0, rowSpan = 1, columnIndex = index, columnSpan = 1)
          customActions =
            listOfNotNull(
              previous?.let {
                CustomAccessibilityAction(previousLabel) {
                  actions.onSelect(it.id)
                  true
                }
              },
              next?.let {
                CustomAccessibilityAction(nextLabel) {
                  actions.onSelect(it.id)
                  true
                }
              },
            )
        },
  ) {
    Crossfade(targetState = selected, animationSpec = tween(ExpandMillis / 2), label = "resultContent") { open ->
      if (open) {
        ResultForm(state = state, result = result, actions = actions)
      } else {
        ResultSummary(
          state = state,
          result = result,
          onRemove = if (state.results.size > 1) ({ actions.onRemove(result.id) }) else null,
        )
      }
    }
  }
}

/** A folded result: shape, name, «12 units · Category», the frozen pill and a dot when it still lacks something. */
@Composable
private fun ResultSummary(
  state: TransformState,
  result: ResultUi,
  /** Null when it is the only result: it cannot be removed. */
  onRemove: (() -> Unit)?,
) {
  val category = state.categories.firstOrNull { it.id == state.categoryOf(result) }
  val name = result.name.ifBlank { stringResource(R.string.transform_result_unnamed) }
  Box {
    ResultSummaryContent(
      state = state,
      result = result,
      category = category,
      name = name,
      reserveClose =
        onRemove != null,
    )
    if (onRemove != null) {
      IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd)) {
        Icon(
          Symbols.Outlined.Close,
          contentDescription = stringResource(R.string.transform_result_delete),
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun ResultSummaryContent(
  state: TransformState,
  result: ResultUi,
  category: Category?,
  name: String,
  reserveClose: Boolean,
) {
  Column(
    modifier =
      Modifier
        .height(ResultCardHeight)
        .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = if (reserveClose) 36.dp else 10.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
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
