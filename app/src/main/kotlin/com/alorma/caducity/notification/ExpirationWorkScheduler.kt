package com.alorma.caducity.notification

interface ExpirationWorkScheduler {
  /**
   * Schedules periodic expiration checks.
   * Work will run once every 24 hours with a 15-minute flex period.
   * Uses KEEP policy to avoid rescheduling if work is already scheduled.
   */
  fun scheduleExpirationCheck()

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