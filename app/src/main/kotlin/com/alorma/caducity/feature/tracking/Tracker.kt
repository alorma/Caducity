package com.alorma.caducity.feature.tracking

/**
 * Interface for tracking analytics events.
 * Implementations should handle tracking to their respective analytics platforms.
 */
interface Tracker {
  /**
   * Track a screen view event.
   *
   * @param screen The screen being viewed
   */
  fun trackScreen(screen: Screen)

  /**
   * Track a user action event.
   *
   * @param action The action being performed
   */
  fun trackAction(action: Action)
}
