package com.alorma.caducity.feature.tracking

import timber.log.Timber

/**
 * Tracker implementation that logs events to the console using Timber.
 *
 * This tracker is primarily useful for:
 * - Debugging during development
 * - Verifying tracking calls in debug builds
 * - Testing tracking behavior without external dependencies
 *
 * Events are logged at the DEBUG level with descriptive messages.
 */
class TimberTracker : Tracker {
  override fun trackScreen(screen: Screen) {
    Timber.d("Screen tracked: ${screen.name}")
  }

  override fun trackAction(action: Action) {
    val parametersString = if (action.parameters.isNotEmpty()) {
      action.parameters.entries.joinToString(", ") { "${it.key}=${it.value}" }
    } else {
      "no parameters"
    }
    Timber.d("Action tracked: ${action.name} ($parametersString)")
  }
}
