package com.alorma.caducity.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

interface ThemePreferences {
  val themeMode: MutableState<ThemeMode>
  val useDynamicColors: MutableState<Boolean>
  val tonalColorMode: MutableState<TonalColorMode>

  fun loadThemeMode(): ThemeMode
  fun loadUseDynamicColors(): Boolean
  fun loadTonalColorMode(): TonalColorMode
  fun setThemeModeState(mode: ThemeMode)
  fun setDynamicColorsEnabled(enabled: Boolean)
  fun setTonalColorMode(mode: TonalColorMode)
}

object ThemePreferencesNoOp : ThemePreferences {
  override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
  override val useDynamicColors: MutableState<Boolean> = mutableStateOf(true)
  override val tonalColorMode: MutableState<TonalColorMode> = mutableStateOf(TonalColorMode.VIBRANT)

  override fun loadThemeMode(): ThemeMode {
    return themeMode.value
  }

  override fun loadUseDynamicColors(): Boolean {
    return useDynamicColors.value
  }

  override fun loadTonalColorMode(): TonalColorMode {
    return tonalColorMode.value
  }

  override fun setThemeModeState(mode: ThemeMode) {

  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {

  }

  override fun setTonalColorMode(mode: TonalColorMode) {

  }
}

object ThemePreferencesScreenshotTestNoOp : ThemePreferences {
  override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
  override val useDynamicColors: MutableState<Boolean> = mutableStateOf(true)
  override val tonalColorMode: MutableState<TonalColorMode> = mutableStateOf(TonalColorMode.VIBRANT)

  override fun loadThemeMode(): ThemeMode {
    return themeMode.value
  }

  override fun loadUseDynamicColors(): Boolean {
    return useDynamicColors.value
  }

  override fun loadTonalColorMode(): TonalColorMode {
    return tonalColorMode.value
  }

  override fun setThemeModeState(mode: ThemeMode) {

  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {

  }

  override fun setTonalColorMode(mode: TonalColorMode) {

  }
}