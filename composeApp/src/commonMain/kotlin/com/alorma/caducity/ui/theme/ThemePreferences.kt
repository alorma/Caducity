package com.alorma.caducity.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

interface ThemePreferences {
  val themeMode: MutableState<ThemeMode>
  val useDynamicColors: MutableState<Boolean>

  fun loadThemeMode(): ThemeMode
  fun loadUseDynamicColors(): Boolean
  fun setThemeModeState(mode: ThemeMode)
  fun setDynamicColorsEnabled(enabled: Boolean)
}

object ThemePreferencesNoOp : ThemePreferences {
  override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
  override val useDynamicColors: MutableState<Boolean> = mutableStateOf(true)

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