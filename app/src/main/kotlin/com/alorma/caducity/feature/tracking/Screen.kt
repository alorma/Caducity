package com.alorma.caducity.feature.tracking

/**
 * Base class for screen tracking events.
 * Represents a screen view event that can be tracked.
 *
 * @property name The name of the screen being tracked
 */
data class Screen(
  val name: String,
)
