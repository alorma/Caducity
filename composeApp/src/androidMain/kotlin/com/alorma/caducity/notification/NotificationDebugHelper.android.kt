package com.alorma.caducity.notification

import com.alorma.caducity.worker.ExpirationWorkScheduler

/**
 * Android implementation of NotificationDebugHelper.
 * Uses ExpirationWorkScheduler to trigger immediate notification checks.
 */
actual class NotificationDebugHelper(
  private val workScheduler: ExpirationWorkScheduler
) {
  actual fun triggerImmediateCheck() {
    workScheduler.triggerImmediateCheck()
  }
}
