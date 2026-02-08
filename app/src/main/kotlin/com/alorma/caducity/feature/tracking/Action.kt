package com.alorma.caducity.feature.tracking

/**
 * Base class representing a user action tracking event.
 *
 * Use this class to track user interactions, button clicks, and other actions within the app.
 * Actions can include optional parameters to provide additional context.
 *
 * @property name The unique identifier/name of the action being tracked
 * @property parameters Optional key-value pairs providing additional context for the action
 */
data class Action(
  val name: String,
  val parameters: Map<String, String> = emptyMap(),
)
