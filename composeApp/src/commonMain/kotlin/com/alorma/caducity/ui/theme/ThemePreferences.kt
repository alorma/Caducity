package com.alorma.caducity.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ThemePreferences {
  var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    private set

  var useDynamicColors by mutableStateOf(false)
    private set

  fun setThemeMode(mode: ThemeMode) {
    themeMode = mode
  }

  fun setUseDynamicColors(enabled: Boolean) {
    useDynamicColors = enabled
  }
}
