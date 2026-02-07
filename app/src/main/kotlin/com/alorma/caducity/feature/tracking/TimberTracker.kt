package com.alorma.caducity.feature.tracking

import timber.log.Timber

/**
 * Tracker implementation that logs events using Timber.
 * Useful for debugging and development purposes.
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
