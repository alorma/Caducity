package com.alorma.caducity.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

interface ThemePreferences {
  val themeMode: MutableState<ThemeMode>
  val useDynamicColors: MutableState<Boolean>

  val useAppFonts: MutableState<Boolean>

  fun loadThemeMode(): ThemeMode
  fun loadUseDynamicColors(): Boolean
  fun setThemeModeState(mode: ThemeMode)
  fun setDynamicColorsEnabled(enabled: Boolean)
}

object ThemePreferencesNoOp : ThemePreferences {
  override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
  override val useDynamicColors: MutableState<Boolean> = mutableStateOf(true)
  override val useAppFonts: MutableState<Boolean> = mutableStateOf(true)

  override fun loadThemeMode(): ThemeMode {
    return themeMode.value
  }

  override fun loadUseDynamicColors(): Boolean {
    return useDynamicColors.value
  }

  override fun setThemeModeState(mode: ThemeMode) {

  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {

  }
}

object ThemePreferencesScreenshotTestNoOp : ThemePreferences {
  override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
  override val useDynamicColors: MutableState<Boolean> = mutableStateOf(true)
  override val useAppFonts: MutableState<Boolean> = mutableStateOf(false)

  override fun loadThemeMode(): ThemeMode {
    return themeMode.value
  }

  override fun loadUseDynamicColors(): Boolean {
    return useDynamicColors.value
  }

  override fun setThemeModeState(mode: ThemeMode) {

  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {

  }
}