package com.alorma.caducity.settings

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private var appContext: Context? = null

fun initializeSettings(context: Context) {
  appContext = context.applicationContext
}

actual fun createSettings(): Settings {
  val context = appContext ?: error("Settings must be initialized by calling initializeSettings(context) before use")
  val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
  return SharedPreferencesSettings(sharedPreferences)
}
