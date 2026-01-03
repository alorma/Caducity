package com.alorma.caducity.ui.theme

import androidx.compose.runtime.MutableState

interface ThemePreferences {
  val themeMode: MutableState<ThemeMode>
  val useDynamicColors: MutableState<Boolean>

  fun loadThemeMode(): ThemeMode
  fun loadUseDynamicColors(): Boolean
  fun setThemeModeState(mode: ThemeMode)
  fun setDynamicColorsEnabled(enabled: Boolean)
}