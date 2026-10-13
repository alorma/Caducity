package com.alorma.caducity.ui.screen.transform

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Symbols
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
private const val DrawMillis = 350

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
  LaunchedEffect(state.expandedResultId, state.results.size) {
    val index = state.results.indexOfFirst { it.id == state.expandedResultId }
    if (index < 0) return@LaunchedEffect
    listState.animateScrollToItem(index)
    // The cards around it are still changing size while the first scroll runs (the card itself waits for the drop
    // to be undrawn first): align once they have settled.
    delay((DrawMillis + ExpandMillis).toLong())
    listState.animateScrollToItem(index)
  }
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SectionSpacing)) {
    RowSectionHeader(
      title = stringResource(R.string.transform_section_results_count, state.results.size),
      onAdd = if (state.canAddResult) actions.onAdd else null,
      center = { RowLinksNode(links) },
    )
    SideEffect {
      links?.resultsState = listState
    }
    BoxWithConstraints {
      val formWidth = maxWidth - ScreenGutter * 2
      LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = ScreenGutter),
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
            selected = result.id == state.expandedResultId,
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
  // What is on screen follows [selected]: on the way shut at once; on the way open only once the drop has been
  // undrawn (it reports it), or at once when there is no drop.
  var showForm by remember { mutableStateOf(selected) }
  val hasDrop = state.results.size > 1
  LaunchedEffect(selected) {
    if (!selected) {
      showForm = false
    } else if (!hasDrop || showForm) {
      showForm = true
    }
  }
  val width by animateDpAsState(
    targetValue = if (showForm) formWidth else RowCardWidth,
    animationSpec = tween(ExpandMillis, easing = EmphasizedEasing),
    label = "resultWidth",
  )
  // A folded result that still lacks something is outlined, so it is not mistaken for a finished one.
  val incomplete = !showForm && !state.isValid(result)
  val invalidLabel = stringResource(R.string.transform_result_invalid)
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Surface(
      shape = RowCardShape,
      color = CaducityTheme.colorScheme.surfaceContainer,
      border =
        when {
          showForm -> BorderStroke(2.dp, CaducityTheme.colorScheme.primary)
          incomplete -> BorderStroke(1.5.dp, CaducityTheme.colorScheme.error)
          else -> null
        },
      modifier =
        Modifier
          .width(width)
          .animateContentSize(tween(ExpandMillis, easing = EmphasizedEasing))
          .clickable(enabled = !selected, role = Role.Button) { actions.onSelect(result.id) }
          .semantics {
            this.selected = selected
            if (incomplete) stateDescription = invalidLabel
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
      Crossfade(targetState = showForm, animationSpec = tween(ExpandMillis / 2), label = "resultContent") { open ->
        if (open) {
          ResultForm(state = state, result = result, actions = actions)
        } else {
          ResultSummary(state = state, result = result)
        }
      }
    }
    if (!showForm && hasDrop) {
      HangingRemove(
        visible = !selected,
        onHidden = { showForm = true },
        onRemove = { actions.onRemove(result.id) },
      )
    }
  }
}

private val DropWidth = 40.dp
private val TailHeight = 66.dp
private val TailStroke = 1.5.dp

/** How far the tail goes up into the card, to cover the card's own border where the neck opens into it. */
private val TailOverlap = 2.dp

/**
 * The outline that hangs from the bottom edge of a folded card (y = 0 is that edge) and continues the card's shape:
 * the edge bends down into a narrow neck that widens into a round drop. With [withFillet] it starts and ends on the
 * card's edge; without it, only the neck and the drop (the tappable part). [coverCard] closes the path over the card
 * border so it can be filled. Every measure is a share of [width], so the shape and the drawing always agree.
 */
private fun tailPath(
  width: Float,
  height: Float,
  withFillet: Boolean,
  coverCard: Boolean = false,
  overlap: Float = 0f,
): Path =
  Path().apply {
    val centre = width / 2f
    val neck = width * 0.0625f
    val neckHeight = width * 0.25f
    val fillet = width * 0.3f
    val radius = width / 2f
    val centreY = height - radius
    val reach = (centreY - neckHeight) * 0.25f
    if (withFillet) {
      moveTo(centre - fillet, 0f)
      cubicTo(centre - fillet * 0.45f, 0f, centre - neck, neckHeight * 0.45f, centre - neck, neckHeight)
    } else {
      moveTo(centre - neck, neckHeight)
    }
    cubicTo(centre - neck, neckHeight + reach, 0f, centreY - radius * 0.7f, 0f, centreY)
    arcTo(Rect(0f, height - width, width, height), 180f, -180f, false)
    cubicTo(width, centreY - radius * 0.7f, centre + neck, neckHeight + reach, centre + neck, neckHeight)
    if (withFillet) {
      cubicTo(centre + neck, neckHeight * 0.45f, centre + fillet * 0.45f, 0f, centre + fillet, 0f)
      if (coverCard) {
        lineTo(centre + fillet, -overlap)
        lineTo(centre - fillet, -overlap)
      }
    }
    if (coverCard || !withFillet) close()
  }

private val DropShape = GenericShape { size, _ -> addPath(tailPath(size.width, size.height, withFillet = false)) }

/**
 * A short line that hangs from a folded card and ends in a drop with a «×» inside: the card's way to be removed.
 * The whole drop is the button, with a reddish ripple.
 */
@Composable
private fun HangingRemove(
  visible: Boolean,
  /** Called when it has been undrawn after [visible] went false: the card can expand now. */
  onHidden: () -> Unit,
  onRemove: () -> Unit,
) {
  val error = CaducityTheme.colorScheme.error
  val cardColor = CaducityTheme.colorScheme.surfaceContainer
  // It grows out of the card once the card has settled after folding, and shrinks back before the card expands.
  val progress = remember { Animatable(0f) }
  LaunchedEffect(visible) {
    if (visible) delay(ExpandMillis.toLong())
    progress.animateTo(if (visible) 1f else 0f, tween(DrawMillis, easing = LinearEasing))
    if (!visible) onHidden()
  }
  // The space is always reserved, so nothing around it moves while the card is still changing size.
  Box(
    modifier =
      Modifier
        .size(width = DropWidth, height = TailHeight)
        .drawBehind {
          val stroke = TailStroke.toPx()
          val path =
            tailPath(
              width = size.width - stroke,
              height = size.height - stroke / 2,
              withFillet = true,
              coverCard = true,
              overlap = TailOverlap.toPx(),
            )
          // The card's border is drawn inside its edge: start on its centre line so the two strokes meet without a step.
          translate(left = stroke / 2, top = -stroke / 2) {
            scale(scaleX = 1f, scaleY = progress.value, pivot = Offset(size.width / 2, 0f)) {
              drawPath(path, cardColor)
              val open = tailPath(width = size.width - stroke, height = size.height - stroke / 2, withFillet = true)
              drawPath(open, error, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
          }
        },
    contentAlignment = Alignment.BottomCenter,
  ) {
    Box(
      modifier =
        Modifier
          .size(width = DropWidth, height = TailHeight)
          .clip(DropShape)
          .clickable(
            enabled = progress.value >= 1f,
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = error),
            role = Role.Button,
            onClick = onRemove,
          ),
      contentAlignment = Alignment.BottomCenter,
    ) {
      Icon(
        Symbols.Outlined.Close,
        contentDescription = stringResource(R.string.transform_result_delete),
        tint = error,
        // The centre of the round body, which is half the width above the bottom.
        modifier = Modifier.padding(bottom = DropWidth / 2 - 10.dp).size(20.dp).alpha(progress.value),
      )
    }
  }
}

/** A folded result: shape, name, «12 units · Category» and the frozen pill. */
@Composable
private fun ResultSummary(
  state: TransformState,
  result: ResultUi,
) {
  val category = state.categories.firstOrNull { it.id == state.categoryOf(result) }
  val name = result.name.ifBlank { stringResource(R.string.transform_result_unnamed) }
  Column(
    modifier = Modifier.height(ResultCardHeight).padding(10.dp),
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
