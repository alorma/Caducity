package com.alorma.caducity.config.version

import android.content.Context

/**
 * Android implementation of AppVersionProvider.
 * Retrieves version information from PackageManager.
 */
class AndroidAppVersionProvider(
  private val context: Context
) : AppVersionProvider {
  override fun getVersionName(): String {
    return try {
      context.packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName ?: "Unknown"
    } catch (e: Exception) {
      "Unknown"
    }
  }

  override fun getVersionCode(): Int {
    return try {
      context.packageManager
        .getPackageInfo(context.packageName, 0)
        .longVersionCode.toInt()
    } catch (e: Exception) {
      0
    }
  }
}