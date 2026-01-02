package com.alorma.caducity.feature.debug

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Android implementation of DebugModeProvider.
 * Checks if the application is debuggable via ApplicationInfo flags.
 */
class AndroidDebugModeProvider(
  private val context: Context
) : DebugModeProvider {
  override fun isDebugMode(): Boolean {
    return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
  }
}
