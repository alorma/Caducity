package com.alorma.caducity.feature.notification

import kotlinx.datetime.LocalTime

interface ExpirationWorkScheduler {
  /**
   * Schedules periodic expiration checks at the given [time] of day.
   * Work will run once every 24 hours starting at the next occurrence of [time].
   * Uses KEEP policy to avoid rescheduling if work is already scheduled.
   * Default is noon (12:00) when no prior schedule exists.
   */
  fun scheduleExpirationCheck(time: LocalTime = LocalTime(12, 0))

  /**
   * Cancels any existing scheduled work and re-schedules at the given [time].
   * Use this when the user changes their notification time preference.
   */
  fun rescheduleExpirationCheck(time: LocalTime)

  /**
   * Cancels all scheduled expiration check work.
   * Useful for testing or when user disables notifications.
   */
  fun cancelExpirationCheck()

  /**
   * Triggers an immediate expiration check for testing purposes.
   * This will run the worker immediately without waiting for the scheduled time.
   */
  fun triggerImmediateCheck()
}
