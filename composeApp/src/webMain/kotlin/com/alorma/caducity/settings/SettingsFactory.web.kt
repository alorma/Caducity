package com.alorma.caducity.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.localStorage

actual fun createSettings(): Settings {
  return StorageSettings(localStorage)
}
