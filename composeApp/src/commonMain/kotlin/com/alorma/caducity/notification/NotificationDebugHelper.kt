package com.alorma.caducity.notification

/**
 * Platform-specific helper for debugging notifications.
 * Provides methods to manually trigger notification checks for testing.
 */
interface NotificationDebugHelper {
  /**
   * Triggers an immediate notification check for testing purposes.
   * On Android, this will run the WorkManager worker immediately.
   * On other platforms, this may be a no-op.
   */
  fun triggerImmediateCheck()
}
