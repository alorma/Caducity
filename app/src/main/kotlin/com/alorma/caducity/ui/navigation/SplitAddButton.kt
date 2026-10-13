package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.ExpandLess
import com.alorma.caducity.base.ui.icons.symbols.ExpandMore
import com.alorma.caducity.base.ui.icons.symbols.Symbols

private val Outer = 20.dp
private val Inner = 6.dp

/** The «+» FAB on its own: opens «Afegeix producte». */
@Composable
fun AddButton(onClick: () -> Unit) {
  FloatingActionButton(
    onClick = onClick,
    modifier = Modifier.size(64.dp),
    shape = RoundedCornerShape(Outer),
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
  ) {
    AddIcon()
  }
}

/**
 * «+ ⌄»: the «+» part still adds a product, the «⌄» part (40×64) opens a menu of more actions. For now the
 * menu only has Transforma; it is meant to grow. Shown only when [AddButton] would hide a feature behind a flag.
 */
@Composable
fun SplitAddButton(
  onAddClick: () -> Unit,
  onTransformClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  var anchor by remember { mutableStateOf(Rect.Zero) }
  val colors = MaterialTheme.colorScheme
  // The Popup adds a layout node: inside this Box it cannot move the button within the bar.
  Box {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
      FloatingActionButton(
        onClick = onAddClick,
        modifier = Modifier.size(64.dp),
        shape = RoundedCornerShape(topStart = Outer, bottomStart = Outer, topEnd = Inner, bottomEnd = Inner),
        containerColor = colors.primary,
        contentColor = colors.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
      ) {
        AddIcon()
      }
      val moreActions = stringResource(R.string.action_more_actions)
      val expandedText = stringResource(R.string.state_expanded)
      val collapsedText = stringResource(R.string.state_collapsed)
      FloatingActionButton(
        onClick = { expanded = !expanded },
        modifier =
          Modifier
            .width(40.dp)
            .height(64.dp)
            .onGloballyPositioned { anchor = it.boundsInWindow() }
            .semantics {
              contentDescription = moreActions
              role = Role.Button
              stateDescription = if (expanded) expandedText else collapsedText
            },
        shape = RoundedCornerShape(topStart = Inner, bottomStart = Inner, topEnd = Outer, bottomEnd = Outer),
        containerColor = if (expanded) colors.onPrimaryContainer else colors.primary,
        contentColor = colors.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
      ) {
        Icon(
          imageVector = if (expanded) Symbols.Outlined.ExpandMore else Symbols.Outlined.ExpandLess,
          contentDescription = null,
          modifier = Modifier.size(24.dp),
        )
      }
    }
    if (expanded) {
      MoreActionsMenu(
        anchor = anchor,
        onDismiss = { expanded = false },
        onTransform = {
          expanded = false
          onTransformClick()
        },
      )
    }
  }
}

@Composable
private fun AddIcon() {
  Icon(
    imageVector = Symbols.Outlined.Add,
    contentDescription = stringResource(R.string.action_add_product),
    modifier = Modifier.size(28.dp),
  )
}

/** Window-sized popup: 45 % scrim plus the actions right-aligned above the [anchor]. */
@Composable
private fun MoreActionsMenu(
  anchor: Rect,
  onDismiss: () -> Unit,
  onTransform: () -> Unit,
) {
  Popup(
    popupPositionProvider = WindowOrigin,
    onDismissRequest = onDismiss,
    properties = PopupProperties(focusable = true, clippingEnabled = false),
  ) {
    BoxWithConstraints(
      modifier =
        Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.45f))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onDismiss,
          ),
    ) {
      val density = LocalDensity.current
      val end = with(density) { (constraints.maxWidth - anchor.right).coerceAtLeast(0f).toDp() }
      val bottom = with(density) { (constraints.maxHeight - anchor.top).coerceAtLeast(0f).toDp() + 12.dp }
      Surface(
        onClick = onTransform,
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier =
          Modifier
            .align(Alignment.BottomEnd)
            .padding(end = end, bottom = bottom)
            .height(60.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 24.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Icon(AppIcons.Cooking, contentDescription = null, modifier = Modifier.size(28.dp))
          Text(
            text = stringResource(R.string.transform_title),
            style = MaterialTheme.typography.titleMedium,
          )
        }
      }
    }
  }
}

private object WindowOrigin : PopupPositionProvider {
  override fun calculatePosition(
    anchorBounds: androidx.compose.ui.unit.IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize,
  ): IntOffset = IntOffset.Zero
}
