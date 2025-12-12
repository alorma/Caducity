package com.alorma.caducity.ui.theme

interface ThemePreferences {
  fun loadThemeMode(): ThemeMode
  fun loadUseDynamicColors(): Boolean
  fun setThemeModeState(mode: ThemeMode)
  fun setDynamicColorsEnabled(enabled: Boolean)
}

object ThemePreferencesNoOp : ThemePreferences {
  override fun loadThemeMode(): ThemeMode {
    return ThemeMode.SYSTEM
  }

  override fun loadUseDynamicColors(): Boolean {
    return true
  }

  override fun setThemeModeState(mode: ThemeMode) {

  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {

  }

}