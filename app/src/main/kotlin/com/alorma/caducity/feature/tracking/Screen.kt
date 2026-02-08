package com.alorma.caducity.feature.tracking

/**
 * Base class representing a screen view tracking event.
 *
 * Use this class to track when users navigate to different screens in the app.
 * The screen name should be descriptive and consistent across the app.
 *
 * @property name The unique identifier/name of the screen being tracked
 */
abstract class Screen(
  val name: String,
)
