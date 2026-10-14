package com.alorma.caducity.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy

// Same transitions as Converses (ui/AppNavigation.kt), plus the fade between tabs.

private const val TransitionMs = 300
private const val FadeMs = 150
private const val BackScale = 0.9f
private const val EnterStartAlpha = 0.6f
private const val ExitEndAlpha = 0.4f

private val transitionEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/**
 * Transitions close to the native Android ones: the new screen slides in a little; going back,
 * only the leaving screen shrinks and fades out, revealing the previous one (like predictive back).
 * Modal screens (Add, Transform, editors) slide in vertically and leave the screen below untouched.
 */
internal fun <S> AnimatedContentTransitionScope<S>.nativeTransition(
  forward: Boolean,
  vertical: Boolean,
): ContentTransform {
  val offset = tween<IntOffset>(TransitionMs, easing = transitionEasing)

  fun distance(size: Int) = size / 12
  val enter: EnterTransition
  val exit: ExitTransition
  if (forward) {
    enter = (
      if (vertical) slideInVertically(offset, ::distance) else slideInHorizontally(offset, ::distance)
    ) + fadeIn(tween(TransitionMs, easing = LinearEasing), initialAlpha = EnterStartAlpha)
    exit = ExitTransition.None
  } else {
    enter = EnterTransition.None
    exit =
      if (vertical) {
        slideOutVertically(offset, ::distance) +
          fadeOut(tween(FadeMs + 100, easing = LinearEasing), targetAlpha = ExitEndAlpha)
      } else {
        scaleOut(tween(TransitionMs, easing = transitionEasing), targetScale = BackScale) +
          fadeOut(tween(FadeMs + 100, easing = LinearEasing), targetAlpha = ExitEndAlpha)
      }
  }
  return enter togetherWith exit
}

private const val EdgeRight = 1
private const val GestureScale = 0.85f

/**
 * Back gesture (seeked by the finger): like the system animation, the leaving screen shrinks and
 * moves towards the side the gesture comes from, and only fades at the very end. The screen below
 * stays in place.
 */
internal fun <S> AnimatedContentTransitionScope<S>.predictiveBack(
  edge: Int,
  vertical: Boolean,
): ContentTransform {
  val offset = tween<IntOffset>(TransitionMs, easing = LinearOutSlowInEasing)
  val side = if (edge == EdgeRight) -1 else 1
  val slide =
    if (vertical) {
      slideOutVertically(offset) { it / 8 }
    } else {
      slideOutHorizontally(offset) { side * it / 8 }
    }
  val exit =
    slide +
      scaleOut(tween(TransitionMs, easing = LinearOutSlowInEasing), targetScale = GestureScale) +
      fadeOut(tween(TransitionMs / 3, delayMillis = TransitionMs * 2 / 3, easing = LinearEasing))
  return EnterTransition.None togetherWith exit
}

/** Fade through: the leaving tab fades out quickly, then the new one fades in. */
private fun <S> AnimatedContentTransitionScope<S>.tabTransition(): ContentTransform =
  fadeIn(tween(TransitionMs - FadeMs / 2, delayMillis = FadeMs / 2, easing = LinearEasing)) togetherWith
    fadeOut(tween(FadeMs / 2, easing = LinearEasing))

/** Metadata for the modal screens (Add, Transform, editors): vertical transitions. */
internal val modalTransitions =
  NavDisplay.transitionSpec { nativeTransition(forward = true, vertical = true) } +
    NavDisplay.popTransitionSpec { nativeTransition(forward = false, vertical = true) } +
    NavDisplay.predictivePopTransitionSpec { edge -> predictiveBack(edge, vertical = true) }

/**
 * Metadata for every tab but the base: going to it (`[Home]` → `[Home, Pantry]`) and leaving it from
 * the bar (`[Home, Pantry]` → `[Home]`) fade. The back gesture keeps the predictive animation.
 */
internal val tabTransitions =
  NavDisplay.transitionSpec { tabTransition() } +
    NavDisplay.popTransitionSpec { tabTransition() }

/**
 * Paints the theme background under every screen (but the bottom sheets, which float over the previous
 * one). With a single stack the screen below stays composed, so without it it shows through the
 * leaving screen while it shrinks or fades.
 */
@Composable
internal fun <T : Any> rememberOpaqueBackgroundNavEntryDecorator(): NavEntryDecorator<T> {
  val background = MaterialTheme.colorScheme.background
  return remember(background) {
    NavEntryDecorator { entry ->
      if (entry.metadata.containsKey(BottomSheetSceneStrategy.BOTTOM_SHEET_KEY)) {
        entry.Content()
      } else {
        Box(Modifier.fillMaxSize().background(background)) { entry.Content() }
      }
    }
  }
}
