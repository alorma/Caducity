package com.alorma.caducity.feature.tracking

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

/**
 * Tracker implementation that sends events to Firebase Analytics.
 *
 * @property analytics Firebase Analytics instance
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
