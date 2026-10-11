package com.alorma.caducity.ui.screen.status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.ArrowBack
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.ui.adaptive.rememberIsExpanded
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors

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

private val CollapsedHeight = 72.dp

/**
 * Header of the status lists. Unfolded: back, overline, big title, help text and buttons with text.
 * Folded (72 dp): back, small title with the count below and icon buttons, same container color,
 * lower radius 24 and a soft shadow so the list can be seen going under it.
 */
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
  // The selection bar replaces the header, folded or not.
  val fraction = if (selectionCount != null) 1f else state.fraction
  // The color goes edge to edge, the content stays in the same column as the list below.
  val contentMaxWidth = if (rememberIsExpanded()) 600.dp else Dp.Unspecified
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = colors.container,
    contentColor = colors.onContainer,
    shape = RoundedCornerShape(bottomStart = lerp(32.dp, 24.dp, fraction), bottomEnd = lerp(32.dp, 24.dp, fraction)),
    shadowElevation = lerp(0.dp, 2.dp, fraction),
  ) {
    Layout(
      content = {
        Column(
          modifier =
            Modifier
              .widthIn(max = contentMaxWidth)
              .fillMaxWidth()
              .alpha((1f - 2f * fraction).coerceIn(0f, 1f))
              .windowInsetsPadding(TopAppBarDefaults.windowInsets),
        ) {
          UnfoldedHeader(count, title, help, actions, colors.onContainer, onBack)
        }
        Row(
          modifier =
            Modifier
              .widthIn(max = contentMaxWidth)
              .alpha(
                (2f * fraction - 1f).coerceIn(0f, 1f),
              ).windowInsetsPadding(TopAppBarDefaults.windowInsets),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          FoldedHeader(status, count, title, actions, selectionCount, onBack, onCloseSelection)
        }
      },
    ) { measurables, constraints ->
      val loose = constraints.copy(minWidth = 0, minHeight = 0)
      val unfolded = measurables[0].measure(loose)
      val folded = measurables[1].measure(loose)
      val height = lerpInt(unfolded.height, folded.height, fraction)
      // The folding range is what the unfolded layer has over the folded one.
      state.rangePx = (unfolded.height - folded.height).coerceAtLeast(0).toFloat()
      layout(constraints.maxWidth, height) {
        unfolded.place((constraints.maxWidth - unfolded.width) / 2, 0)
        folded.place((constraints.maxWidth - folded.width) / 2, 0)
      }
    }
  }
}

private fun lerpInt(
  start: Int,
  stop: Int,
  fraction: Float,
): Int = (start + (stop - start) * fraction).toInt()

@Composable
private fun UnfoldedHeader(
  count: Int,
  title: String,
  help: String,
  actions: List<HeaderAction>,
  contentColor: androidx.compose.ui.graphics.Color,
  onBack: () -> Unit,
) {
  IconButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
    Icon(Symbols.Outlined.ArrowBack, contentDescription = stringResource(R.string.calendar_back))
  }
  Column(
    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = pluralStringResource(R.plurals.home_product_count, count, count).uppercase(),
      style = CaducityTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
    )
    Text(
      text = title,
      style = CaducityTheme.typography.displaySmall,
      modifier = Modifier.semantics { heading() },
    )
    Text(text = help, style = CaducityTheme.typography.bodyMedium)
    if (actions.isNotEmpty()) {
      Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        actions.forEach { action -> UnfoldedAction(action, contentColor) }
      }
    }
  }
}

@Composable
private fun UnfoldedAction(
  action: HeaderAction,
  contentColor: androidx.compose.ui.graphics.Color,
) {
  val content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
    Icon(action.icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(8.dp))
    Text(action.label)
  }
  if (action.primary) {
    Button(
      onClick = action.onClick,
      colors =
        ButtonDefaults.buttonColors(
          containerColor = CaducityTheme.colors.expired.strong,
          contentColor = CaducityTheme.colorScheme.surface,
        ),
      modifier = Modifier.heightIn(min = 44.dp),
      content = content,
    )
  } else {
    OutlinedButton(
      onClick = action.onClick,
      border = BorderStroke(1.dp, contentColor),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
      modifier = Modifier.heightIn(min = 44.dp),
      content = content,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun androidx.compose.foundation.layout.RowScope.FoldedHeader(
  status: ExpiryStatus,
  count: Int,
  title: String,
  actions: List<HeaderAction>,
  selectionCount: Int?,
  onBack: () -> Unit,
  onCloseSelection: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .weight(
          1f,
        ).heightIn(min = CollapsedHeight)
        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    if (selectionCount != null) {
      IconButton(onClick = onCloseSelection, modifier = Modifier.size(48.dp)) {
        Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.status_select_done))
      }
      Text(
        text = pluralStringResource(R.plurals.status_selected_count, selectionCount, selectionCount),
        style = CaducityTheme.typography.titleLarge,
        modifier = Modifier.weight(1f).padding(start = 4.dp).semantics { heading() },
      )
    } else {
      IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
        Icon(Symbols.Outlined.ArrowBack, contentDescription = stringResource(R.string.calendar_back))
      }
      Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
        Text(
          text = title,
          style = CaducityTheme.typography.displaySmall.copy(fontSize = 24.sp, lineHeight = 26.sp),
          maxLines = 1,
          modifier = Modifier.semantics { heading() },
        )
        Text(
          text = pluralStringResource(R.plurals.home_product_count, count, count).uppercase(),
          style = CaducityTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
        )
      }
      // The primary action goes first (to the left of «Selecciona»).
      actions.forEach { action ->
        TooltipBox(
          positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
          tooltip = { PlainTooltip { Text(action.label) } },
          state = rememberTooltipState(),
        ) {
          IconButton(
            onClick = action.onClick,
            modifier = Modifier.size(48.dp),
            colors =
              if (action.primary) {
                IconButtonDefaults.iconButtonColors(
                  containerColor = status.colors().strong,
                  contentColor = CaducityTheme.colorScheme.surface,
                )
              } else {
                IconButtonDefaults.iconButtonColors()
              },
          ) {
            Icon(action.icon, contentDescription = action.label)
          }
        }
      }
    }
  }
}
