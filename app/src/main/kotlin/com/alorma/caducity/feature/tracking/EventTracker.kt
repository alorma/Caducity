package com.alorma.caducity.feature.tracking

/**
 * EventTracker that delegates tracking calls to a list of trackers.
 * This allows multiple analytics platforms to be used simultaneously.
 *
 * @property trackers List of trackers to delegate to
 */
class EventTracker(
  private val trackers: List<Tracker>,
) : Tracker {
  override fun trackScreen(screen: Screen) {
    trackers.forEach { tracker ->
      tracker.trackScreen(screen)
    }
  }

  override fun trackAction(action: Action) {
    trackers.forEach { tracker ->
      tracker.trackAction(action)
    }
  }
}
