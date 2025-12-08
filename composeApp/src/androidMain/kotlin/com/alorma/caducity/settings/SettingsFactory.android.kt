package com.alorma.caducity.settings

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private lateinit var appContext: Context

fun initializeSettings(context: Context) {
  appContext = context.applicationContext
}

actual fun createSettings(): Settings {
  val sharedPreferences = appContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
  return SharedPreferencesSettings(sharedPreferences)
}
