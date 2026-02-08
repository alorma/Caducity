package com.alorma.caducity.feature.tracking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.koin.compose.koinInject

/**
 * Tracks screen view events automatically when the lifecycle reaches OnStart.
 *
 * This composable observes the lifecycle of the current screen and triggers
 * [EventTracker.trackScreen] when the lifecycle transitions to ON_START.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *   TrackScreen(screen = Screen(name = "MyScreen"))
 *   // ... rest of screen content
 * }
 * ```
 *
 * @param screen The screen event to track
 * @param eventTracker The tracker instance (injected via Koin)
 */
@Composable
fun TrackScreen(
  screen: Screen,
  eventTracker: EventTracker = koinInject(),
) {
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner, screen) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START) {
        eventTracker.trackScreen(screen)
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
}
