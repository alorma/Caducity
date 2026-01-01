package com.alorma.caducity.notification

/**
 * Android implementation of NotificationDebugHelper.
 * Uses ExpirationWorkScheduler to trigger immediate notification checks.
 */
class AndroidNotificationDebugHelper(
  private val workScheduler: ExpirationWorkScheduler
) : NotificationDebugHelper {
  override fun triggerImmediateCheck() {
    workScheduler.triggerImmediateCheck()
  }
}
