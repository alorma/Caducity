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
abstract class Action(
  val name: String,
  val parameters: Map<String, String> = emptyMap(),
)

/**
 * Base class for navigation actions.
 *
 * Navigation actions automatically prefix the action name with "nav_" to distinguish
 * navigation events from other actions in analytics. All navigation actions include
 * an "origin" parameter to track where the navigation was triggered from.
 *
 * @property actionName The action name (will be prefixed with "nav_")
 * @property origin The screen or component where the navigation originated from
 * @property parameters Optional key-value pairs providing additional context for the navigation
 */
abstract class NavigationAction(
  actionName: String,
  origin: String,
  parameters: Map<String, String> = emptyMap(),
) : Action(
  name = "nav_$actionName",
  parameters = parameters + ("origin" to origin),
)
