package com.alorma.caducity.ui.components.feedback.snackbar

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The global snackbars live in the app shell, over every screen. A screen with something fixed at the bottom
 * (a button bar, a FAB) marks it with [snackbarAnchor] so the snackbar shows above it instead of covering it.
 */
@Stable
class SnackbarAnchor {
  private val clearances = mutableStateMapOf<Any, Dp>()

  /** Distance from the bottom of the window to the top of the highest anchor on screen. */
  val bottomClearance: Dp get() = clearances.values.maxOrNull() ?: 0.dp

  internal fun set(
    key: Any,
    clearance: Dp,
  ) {
    clearances[key] = clearance
  }

  internal fun release(key: Any) {
    clearances.remove(key)
  }
}

val LocalSnackbarAnchor = staticCompositionLocalOf { SnackbarAnchor() }

/** The global snackbar shows above this element while it is on screen. */
fun Modifier.snackbarAnchor(): Modifier = this then SnackbarAnchorElement

private data object SnackbarAnchorElement : ModifierNodeElement<SnackbarAnchorNode>() {
  override fun create() = SnackbarAnchorNode()

  override fun update(node: SnackbarAnchorNode) = Unit

  override fun InspectorInfo.inspectableProperties() {
    name = "snackbarAnchor"
  }
}

private class SnackbarAnchorNode :
  Modifier.Node(),
  GlobalPositionAwareModifierNode,
  CompositionLocalConsumerModifierNode {
  private var registeredIn: SnackbarAnchor? = null

  override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
    val anchor = currentValueOf(LocalSnackbarAnchor)
    val density = requireDensity()
    val rootHeight = coordinates.findRootCoordinates().size.height
    val clearance = rootHeight - coordinates.positionInRoot().y
    registeredIn = anchor
    anchor.set(this, with(density) { clearance.toDp() })
  }

  override fun onDetach() {
    registeredIn?.release(this)
    registeredIn = null
  }
}
