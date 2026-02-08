package com.alorma.caducity.feature.tracking

/**
 * Composite tracker that delegates tracking calls to multiple tracker implementations.
 *
 * This class implements the Composite pattern, allowing multiple analytics platforms
 * to be used simultaneously. All registered trackers receive the same tracking events.
 *
 * Benefits:
 * - Centralized tracking interface for the entire app
 * - Multiple analytics platforms tracked with a single call
 * - Easy to add or remove tracking platforms via dependency injection
 * - Automatic delegation to all registered Tracker implementations via Koin's getAll()
 *
 * @property trackers List of tracker implementations to delegate calls to
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
