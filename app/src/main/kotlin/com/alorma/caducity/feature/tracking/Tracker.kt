package com.alorma.caducity.feature.tracking

/**
 * Interface defining the contract for analytics tracking implementations.
 *
 * Implementations of this interface should handle tracking to their respective
 * analytics platforms (e.g., Firebase Analytics, Timber logging, etc.).
 *
 * This abstraction allows for:
 * - Multiple tracking platforms to be used simultaneously
 * - Easy addition of new tracking platforms without modifying existing code
 * - Testability through mock implementations
 */
interface Tracker {
  /**
   * Tracks a screen view event.
   *
   * Called when a user navigates to a new screen in the app.
   *
   * @param screen The screen being viewed
   */
  fun trackScreen(screen: Screen)

  /**
   * Tracks a user action event.
   *
   * Called when a user performs an action (e.g., button click, form submission).
   *
   * @param action The action being performed, including optional parameters
   */
  fun trackAction(action: Action)
}
