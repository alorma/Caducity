package com.alorma.caducity.feature.tracking

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

/**
 * Tracker implementation that sends events to Firebase Analytics.
 *
 * This tracker:
 * - Sends screen view events using the standard Firebase SCREEN_VIEW event
 * - Sends custom action events with optional parameters
 * - Leverages Firebase BOM for dependency version management
 *
 * All events are automatically uploaded to Firebase based on the Firebase SDK configuration.
 *
 * @property analytics Firebase Analytics instance provided via dependency injection
 */
class FirebaseTracker(
  private val analytics: FirebaseAnalytics,
) : Tracker {
  override fun trackScreen(screen: Screen) {
    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
      param(FirebaseAnalytics.Param.SCREEN_NAME, screen.name)
    }
  }

  override fun trackAction(action: Action) {
    analytics.logEvent(action.name) {
      action.parameters.forEach { (key, value) ->
        param(key, value)
      }
    }
  }
}
