package com.alorma.caducity.feature.notification

import androidx.compose.runtime.MutableState
import com.alorma.caducity.config.navigation.ComposeNavigator

interface ExpirationNotificationHelper : ComposeNavigator<Any> {
  fun areNotificationsEnabled(): MutableState<Boolean>

  /** Turns the daily reminder on (only with the notification permission) or off, scheduling or cancelling the worker. */
  fun setNotificationsEnabled(enabled: Boolean)

  fun hasNotificationPermission(): Boolean

  fun launch()

  fun changeState(enabled: Boolean)

  /** Shows the reminders of the day (each replaces yesterday's) and removes the ones with nothing to say. */
  fun showDailyReminders(reminders: DailyReminders)
}
