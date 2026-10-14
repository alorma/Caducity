package com.alorma.caducity.ui.components.feedback.snackbar

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
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
fun Modifier.snackbarAnchor(): Modifier =
  composed {
    val anchor = LocalSnackbarAnchor.current
    val density = LocalDensity.current
    val key = remember { Any() }
    DisposableEffect(anchor, key) { onDispose { anchor.release(key) } }
    onGloballyPositioned { coordinates ->
      val rootHeight = coordinates.findRootCoordinates().size.height
      val clearance = rootHeight - coordinates.positionInRoot().y
      anchor.set(key, with(density) { clearance.toDp() })
    }
  }
