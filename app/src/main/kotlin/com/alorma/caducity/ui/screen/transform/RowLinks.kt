package com.alorma.caducity.ui.screen.transform

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.symbols.SoupKitchen
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.components.category.toComposeShape
import com.alorma.caducity.ui.theme.CaducityTheme

/** The most lines that leave from (or arrive at) one of the two rows: only the cards whose centre is in view. */
private const val MaxLinesPerRow = 5

/** How fast a line fades while its card moves away from the middle: 1.5 keeps the middle third at full opacity. */
private const val FadeGain = 1.5f

private val NodeSize = 64.dp

/**
 * Where the pieces of the scheme between the two rows are, so a line can join them: the ingredient cards, the node
 * in the «Resultat» header and the result cards. Each section reports its own; [rowLinks] draws.
 */
@Stable
class RowLinks {
  internal var root by mutableStateOf<LayoutCoordinates?>(null)
  internal var ingredientsRow by mutableStateOf<LayoutCoordinates?>(null)
  internal var ingredientsState by mutableStateOf<LazyListState?>(null)

  /** The dashed «Afegeix ingredient» row of the free entry, which stands in for the ingredients. */
  internal var entry by mutableStateOf<LayoutCoordinates?>(null)
  internal var node by mutableStateOf<LayoutCoordinates?>(null)
  internal var resultsRow by mutableStateOf<LayoutCoordinates?>(null)
  internal var resultsState by mutableStateOf<LazyListState?>(null)
}

/**
 * Draws the scheme behind the content it is applied to (the content of the scrolling column):
 * - a thin sand line from each visible ingredient into the node (dotted from the free entry);
 * - with a result expanded, one green vertical line from the node to the form, which is always centred;
 * - with all of them folded, a thin sand line from the node to each visible result card.
 *
 * Lines fade when the mode changes. Compose animations already follow the system «remove animations» setting.
 */
@Composable
fun Modifier.rowLinks(
  links: RowLinks,
  /** A result is open as the form: one green line to it instead of one sand line to each card. */
  expanded: Boolean,
): Modifier {
  val sand = CaducityTheme.colorScheme.outline
  val green = CaducityTheme.colorScheme.primary
  val greenAlpha by animateFloatAsState(if (expanded) 1f else 0f, tween(200), label = "greenLine")
  val foldedAlpha by animateFloatAsState(if (expanded) 0f else 1f, tween(200), label = "foldedLines")
  return this
    .onGloballyPositioned { links.root = it }
    .drawBehind { drawRowLinks(links, sand, green, greenAlpha, foldedAlpha) }
}

/** The circle between the rows that the lines meet in. */
@Composable
internal fun RowLinksNode(
  links: RowLinks?,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .size(NodeSize)
        .clip(CategoryShape.CLOVER8.toComposeShape())
        .background(CaducityTheme.colorScheme.surfaceContainerHigh)
        .onGloballyPositioned { links?.node = it },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      Symbols.Outlined.SoupKitchen,
      contentDescription = null,
      tint = CaducityTheme.colorScheme.primary,
      modifier = Modifier.size(32.dp),
    )
  }
}

private fun DrawScope.drawRowLinks(
  links: RowLinks,
  sand: Color,
  green: Color,
  greenAlpha: Float,
  foldedAlpha: Float,
) {
  val root = links.root?.takeIf { it.isAttached } ?: return
  val node = links.node?.takeIf { it.isAttached } ?: return
  val half = node.size.height / 2f
  val center = root.localPositionOf(node, Offset(node.size.width / 2f, half))
  val nodeTop = Offset(center.x, center.y - half)
  val nodeBottom = Offset(center.x, center.y + half)
  val thin = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)

  // Ingredients → node.
  val ingredients = links.ingredientsRow?.takeIf { it.isAttached }
  val ingredientsState = links.ingredientsState
  val entry = links.entry?.takeIf { it.isAttached }
  if (ingredients != null && ingredientsState != null) {
    visibleCentres(ingredients, ingredientsState, root).forEach { (from, fade) ->
      drawCurve(from, nodeTop, sand.copy(alpha = fade), thin)
    }
  } else if (entry != null) {
    val from = root.localPositionOf(entry, Offset(entry.size.width / 2f, entry.size.height.toFloat()))
    drawLine(
      color = sand,
      start = Offset(nodeTop.x, from.y),
      end = nodeTop,
      strokeWidth = 1.5.dp.toPx(),
      cap = StrokeCap.Round,
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f, 7.dp.toPx())),
    )
  }

  // Node → results.
  val results = links.resultsRow?.takeIf { it.isAttached } ?: return
  val rowTop = root.localPositionOf(results, Offset.Zero).y
  if (greenAlpha > 0f) {
    drawLine(
      color = green.copy(alpha = greenAlpha),
      start = nodeBottom,
      end = Offset(nodeBottom.x, rowTop),
      strokeWidth = 2.5.dp.toPx(),
      cap = StrokeCap.Round,
    )
  }
  val resultsState = links.resultsState
  if (foldedAlpha > 0f && resultsState != null) {
    visibleCentres(results, resultsState, root, atBottom = false).forEach { (to, fade) ->
      drawCurve(nodeBottom, to, sand.copy(alpha = foldedAlpha * fade), thin)
    }
  }
}

/**
 * Points on the bottom (or top) edge of [row] under the visible part of each card, closest to the middle first and
 * at most [MaxLinesPerRow]. Read from the list state, so they follow the scroll. Each comes with
 * its opacity: full around the middle and fading out the closer the card is to leaving the screen.
 */
private fun visibleCentres(
  row: LayoutCoordinates,
  state: LazyListState,
  root: LayoutCoordinates,
  atBottom: Boolean = true,
): List<Pair<Offset, Float>> {
  val info = state.layoutInfo
  val width = row.size.width
  val half = width / 2f
  val y = if (atBottom) row.size.height.toFloat() else 0f
  return info.visibleItemsInfo
    .mapNotNull { item ->
      // A card that is only partly in view still gets its line, to the middle of the part that shows.
      val start = (item.offset + info.beforeContentPadding).toFloat().coerceAtLeast(0f)
      val end = (item.offset + item.size + info.beforeContentPadding).toFloat().coerceAtMost(width.toFloat())
      if (end > start) (start + end) / 2f else null
    }.sortedBy { x -> kotlin.math.abs(x - half) }
    .take(MaxLinesPerRow)
    .map { x ->
      val away = kotlin.math.abs(x - half) / half
      root.localPositionOf(row, Offset(x, y)) to ((1f - away) * FadeGain).coerceIn(0f, 1f)
    }
}

/** A line that leaves [from] straight down and joins [to] straight down too, like a stream. */
private fun DrawScope.drawCurve(
  from: Offset,
  to: Offset,
  color: Color,
  stroke: Stroke,
) {
  val reach = (to.y - from.y) * 0.6f
  val path =
    Path().apply {
      moveTo(from.x, from.y)
      cubicTo(from.x, from.y + reach, to.x, to.y - reach, to.x, to.y)
    }
  drawPath(path, color, style = stroke)
}
