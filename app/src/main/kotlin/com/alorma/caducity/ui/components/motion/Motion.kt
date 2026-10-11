package com.alorma.caducity.ui.components.motion

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** True when the system has animations turned off: every animation must jump to its final state. */
@Composable
fun rememberReducedMotion(): Boolean {
  val resolver = LocalContext.current.contentResolver
  return remember(resolver) {
    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
  }
}

/**
 * Fades the content in while it moves from [offsetY] to its place after [delayMillis] (positive = rises from below,
 * negative = drops from above). With [bouncy] the movement is a spring instead of a timed curve.
 */
@Composable
fun Reveal(
  modifier: Modifier = Modifier,
  delayMillis: Int = 0,
  durationMillis: Int = 520,
  offsetY: Dp = 20.dp,
  bouncy: Boolean = false,
  content: @Composable () -> Unit,
) {
  val reduced = rememberReducedMotion()
  val progress = remember { Animatable(if (reduced) 1f else 0f) }
  LaunchedEffect(reduced) {
    if (reduced) {
      progress.snapTo(1f)
    } else {
      delay(delayMillis.toLong())
      if (bouncy) {
        progress.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
      } else {
        progress.animateTo(1f, tween(durationMillis, easing = FastOutSlowInEasing))
      }
    }
  }
  val offsetPx = with(LocalDensity.current) { offsetY.toPx() }
  Box(
    modifier =
      modifier.graphicsLayer {
        alpha = progress.value.coerceIn(0f, 1f)
        translationY = (1f - progress.value) * offsetPx
      },
  ) {
    content()
  }
}
