package com.alorma.caducity.feature.tracking

/**
 * Base class for action tracking events.
 * Represents a user action event that can be tracked.
 *
 * @property name The name of the action being tracked
 * @property parameters Optional parameters associated with the action
 */
data class Action(
  val name: String,
  val parameters: Map<String, String> = emptyMap(),
)
