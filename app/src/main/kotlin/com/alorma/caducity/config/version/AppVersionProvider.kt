package com.alorma.caducity.config.version

/**
 * Platform-specific provider for application version information.
 * Provides access to version name and code from the build configuration.
 */
interface AppVersionProvider {
  /**
   * Returns the application version name (e.g., "1.0").
   * On Android, this comes from versionName in build.gradle.kts.
   */
  fun getVersionName(): String

  /**
   * Returns the application version code.
   * On Android, this comes from versionCode in build.gradle.kts.
   */
  fun getVersionCode(): Int
}
