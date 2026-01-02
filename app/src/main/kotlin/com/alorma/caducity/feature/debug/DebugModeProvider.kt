package com.alorma.caducity.feature.debug

/**
 * Platform-specific provider for detecting debug mode.
 * Provides information about whether the app is running in debug mode.
 */
interface DebugModeProvider {
  /**
   * Returns true if the application is running in debug mode.
   * On Android, this checks if the app is debuggable (ApplicationInfo.FLAG_DEBUGGABLE).
   * On other platforms, this may return false or check platform-specific debug flags.
   */
  fun isDebugMode(): Boolean
}
