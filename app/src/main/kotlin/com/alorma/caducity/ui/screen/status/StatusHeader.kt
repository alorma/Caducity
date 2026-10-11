package com.alorma.caducity.ui.screen.status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.ArrowBack
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.ui.adaptive.rememberIsExpanded
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import kotlin.math.roundToInt

/** A button of the header: text + icon when unfolded, icon only (with tooltip) when folded. */
@Stable
data class HeaderAction(
  val label: String,
  val icon: ImageVector,
  val primary: Boolean = false,
  val onClick: () -> Unit,
)

/** How far the header is folded (0 = unfolded, 1 = folded), driven by the scroll of the list under it. */
@Stable
class StatusHeaderState {
  var offsetPx by mutableFloatStateOf(0f)
  var rangePx by mutableFloatStateOf(0f)
  var enabled by mutableStateOf(true)

  val fraction: Float get() = if (rangePx <= 0f) 0f else (offsetPx / rangePx).coerceIn(0f, 1f)

  /**
   * Scrolling up folds the header first and the list moves afterwards; scrolling down at the top of the
   * list unfolds it. A list that fits the screen never folds it.
   */
  fun connection(listState: LazyListState): NestedScrollConnection =
    object : NestedScrollConnection {
      override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
      ): Offset {
        if (!enabled || available.y >= 0f || rangePx <= 0f) return Offset.Zero
        if (offsetPx <= 0f && !listState.canScrollForward) return Offset.Zero
        val new = (offsetPx - available.y).coerceIn(0f, rangePx)
        val delta = new - offsetPx
        offsetPx = new
        return Offset(0f, -delta)
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
      ): Offset {
        if (!enabled || available.y <= 0f || offsetPx <= 0f) return Offset.Zero
        val new = (offsetPx - available.y).coerceAtLeast(0f)
        val delta = offsetPx - new
        offsetPx = new
        return Offset(0f, delta)
      }
    }
}

@Composable
fun rememberStatusHeaderState(): StatusHeaderState = remember { StatusHeaderState() }

private val CollapsedBarHeight = 72.dp
private val UnfoldedTitleSize = 36.sp
private val FoldedTitleSize = 24.sp

private const val BACK = "back"
private const val OVERLINE = "overline"
private const val TITLE = "title"
private const val HELP = "help"
private const val ACTIONS = "actions"
private const val BAR = "bar"

@OptIn(ExperimentalMotionApi::class)
private fun headerScene(
  topInset: Dp,
  hasActions: Boolean,
): MotionScene =
  MotionScene {
    val back = createRefFor(BACK)
    val overline = createRefFor(OVERLINE)
    val title = createRefFor(TITLE)
    val help = createRefFor(HELP)
    val actions = createRefFor(ACTIONS)
    val bar = createRefFor(BAR)
    val barTop = topInset + 12.dp

    val unfolded =
      constraintSet {
        constrain(bar) {
          top.linkTo(parent.top)
          width = Dimension.fillToConstraints
          height = Dimension.value(0.dp)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
        }
        constrain(back) {
          top.linkTo(parent.top, topInset + 4.dp)
          start.linkTo(parent.start, 4.dp)
        }
        constrain(overline) {
          top.linkTo(back.bottom, 4.dp)
          start.linkTo(parent.start, 24.dp)
        }
        constrain(title) {
          top.linkTo(overline.bottom, 4.dp)
          start.linkTo(parent.start, 24.dp)
          end.linkTo(parent.end, 24.dp)
          width = Dimension.fillToConstraints
          customFontSize("fontSize", UnfoldedTitleSize)
        }
        constrain(help) {
          top.linkTo(title.bottom, 4.dp)
          start.linkTo(parent.start, 24.dp)
          end.linkTo(parent.end, 24.dp)
          width = Dimension.fillToConstraints
          if (!hasActions) bottom.linkTo(parent.bottom, 24.dp)
        }
        constrain(actions) {
          top.linkTo(help.bottom, 12.dp)
          start.linkTo(parent.start, 24.dp)
          if (hasActions) bottom.linkTo(parent.bottom, 24.dp)
        }
      }

    val folded =
      constraintSet {
        constrain(bar) {
          top.linkTo(parent.top)
          width = Dimension.fillToConstraints
          height = Dimension.value(CollapsedBarHeight + topInset)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
        }
        constrain(back) {
          top.linkTo(parent.top, barTop)
          start.linkTo(parent.start, 12.dp)
        }
        constrain(title) {
          top.linkTo(parent.top, barTop - 4.dp)
          start.linkTo(back.end, 4.dp)
          end.linkTo(actions.start, 4.dp)
          width = Dimension.fillToConstraints
          customFontSize("fontSize", FoldedTitleSize)
        }
        constrain(overline) {
          top.linkTo(title.bottom)
          start.linkTo(title.start)
        }
        constrain(help) {
          top.linkTo(title.bottom)
          start.linkTo(title.start)
          end.linkTo(title.end)
          width = Dimension.fillToConstraints
          alpha = 0f
        }
        constrain(actions) {
          top.linkTo(parent.top, barTop)
          end.linkTo(parent.end, 12.dp)
        }
      }

    // The help text leaves early and the count hides while it swaps places with the title, so nothing overlaps.
    defaultTransition(unfolded, folded) {
      keyAttributes(help) {
        frame(25) { alpha = 0f }
      }
      keyAttributes(overline) {
        frame(25) { alpha = 0f }
        frame(75) { alpha = 0f }
      }
    }
  }

/**
 * Header of the status lists, one `MotionLayout` interpolated by the scroll of the list under it.
 * Unfolded: back, overline, big title, help text and buttons with text. Folded (72 dp): back, small
 * title with the count below and icon buttons; the title, the buttons and their labels change size
 * continuously. Same container color, lower radius 24 and a soft shadow so the list goes under it.
 */
@OptIn(ExperimentalMotionApi::class)
@Composable
fun StatusHeader(
  status: ExpiryStatus,
  count: Int,
  title: String,
  help: String,
  actions: List<HeaderAction>,
  state: StatusHeaderState,
  onBack: () -> Unit,
  selectionCount: Int?,
  onCloseSelection: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = status.colors()
  val selecting = selectionCount != null
  // The selection bar replaces the header, folded or not.
  val fraction = if (selecting) 1f else state.fraction
  // The color goes edge to edge, the content stays in the same column as the list below.
  val contentMaxWidth = if (rememberIsExpanded()) 600.dp else Dp.Unspecified
  val topInset = TopAppBarDefaults.windowInsets.asPaddingValues().calculateTopPadding()
  val visibleActions = if (selecting) emptyList() else actions
  val scene = remember(topInset, visibleActions.isNotEmpty()) { headerScene(topInset, visibleActions.isNotEmpty()) }
  val collapsedPx = with(LocalDensity.current) { (CollapsedBarHeight + topInset).toPx() }

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = colors.container,
    contentColor = colors.onContainer,
    shape = RoundedCornerShape(bottomStart = lerp(32.dp, 24.dp, fraction), bottomEnd = lerp(32.dp, 24.dp, fraction)),
    shadowElevation = lerp(0.dp, 2.dp, fraction),
  ) {
    Box(contentAlignment = Alignment.TopCenter) {
      MotionLayout(
        motionScene = scene,
        progress = fraction,
        modifier =
          Modifier
            .widthIn(max = contentMaxWidth)
            .fillMaxWidth()
            .windowInsetsPadding(TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal))
            .onSizeChanged { size ->
              // The unfolded height is what the layout measures while it is not folded at all.
              if (fraction == 0f) state.rangePx = (size.height - collapsedPx).coerceAtLeast(0f)
            },
      ) {
        Box(Modifier.layoutId(BAR))
        IconButton(
          onClick = if (selecting) onCloseSelection else onBack,
          modifier = Modifier.layoutId(BACK).size(48.dp),
        ) {
          Icon(
            if (selecting) Symbols.Outlined.Close else Symbols.Outlined.ArrowBack,
            contentDescription =
              stringResource(if (selecting) R.string.status_select_done else R.string.calendar_back),
          )
        }
        Text(
          text = pluralStringResource(R.plurals.home_product_count, count, count).uppercase(),
          style = CaducityTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
          maxLines = 1,
          modifier = Modifier.layoutId(OVERLINE).alpha(if (selecting) 0f else 1f),
        )
        val titleSize = motionFontSize(TITLE, "fontSize")
        Text(
          text =
            if (selectionCount != null) {
              pluralStringResource(R.plurals.status_selected_count, selectionCount, selectionCount)
            } else {
              title
            },
          style = CaducityTheme.typography.displaySmall.copy(fontSize = titleSize, lineHeight = titleSize * 1.1f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.layoutId(TITLE).semantics { heading() },
        )
        Text(
          text = help,
          style = CaducityTheme.typography.bodyMedium,
          modifier = Modifier.layoutId(HELP),
        )
        Row(
          modifier = Modifier.layoutId(ACTIONS),
          horizontalArrangement = Arrangement.spacedBy(lerp(8.dp, 4.dp, fraction)),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          visibleActions.forEach { action -> HeaderActionButton(action, status, fraction, colors.onContainer) }
        }
      }
    }
  }
}

/** One action: a pill with icon and label that shrinks to a round icon button as [fraction] goes to 1. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderActionButton(
  action: HeaderAction,
  status: ExpiryStatus,
  fraction: Float,
  contentColor: Color,
) {
  val container =
    if (action.primary) {
      lerpColor(CaducityTheme.colors.expired.strong, status.colors().strong, fraction)
    } else {
      Color.Transparent
    }
  val content = if (action.primary) CaducityTheme.colorScheme.surface else contentColor
  val button: @Composable () -> Unit = {
    Surface(
      onClick = action.onClick,
      shape = RoundedCornerShape(50),
      color = container,
      contentColor = content,
      border =
        if (action.primary ||
          fraction >= 1f
        ) {
          null
        } else {
          BorderStroke(1.dp, contentColor.copy(alpha = 1f - fraction))
        },
      modifier = Modifier.heightIn(min = lerp(44.dp, 48.dp, fraction)),
    ) {
      Row(
        modifier =
          Modifier
            .padding(
              horizontal = lerp(16.dp, 12.dp, fraction),
            ).heightIn(min = lerp(44.dp, 48.dp, fraction)),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          action.icon,
          contentDescription = if (fraction > 0.5f) action.label else null,
          modifier = Modifier.size(lerp(18.dp, 24.dp, fraction)),
        )
        Text(
          text = action.label,
          maxLines = 1,
          modifier =
            Modifier
              .clipToBounds()
              .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity))
                val visible = 1f - (fraction * 2f).coerceAtMost(1f)
                val width = (placeable.width * (1f - fraction)).roundToInt()
                val gap = (8.dp.toPx() * (1f - fraction)).roundToInt()
                layout(width + gap, placeable.height) { placeable.placeWithLayer(gap, 0) { alpha = visible } }
              },
        )
      }
    }
  }
  if (fraction > 0.5f) {
    TooltipBox(
      positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
      tooltip = { PlainTooltip { Text(action.label) } },
      state = rememberTooltipState(),
    ) { button() }
  } else {
    button()
  }
}
