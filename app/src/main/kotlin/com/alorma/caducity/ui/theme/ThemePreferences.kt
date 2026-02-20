package com.alorma.caducity.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

interface ThemePreferences {
  val themeMode: MutableState<ThemeMode>
  val useDynamicColors: MutableState<Boolean>
  val themeTone: MutableState<ThemeTone>

  fun loadThemeMode(): ThemeMode
  fun loadUseDynamicColors(): Boolean
  fun loadThemeTone(): ThemeTone
  fun setThemeModeState(mode: ThemeMode)
  fun setDynamicColorsEnabled(enabled: Boolean)
  fun setThemeTone(tone: ThemeTone)
}

object ThemePreferencesNoOp : ThemePreferences {
  override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
  override val useDynamicColors: MutableState<Boolean> = mutableStateOf(true)
  override val themeTone: MutableState<ThemeTone> = mutableStateOf(ThemeTone.VIBRANT)

  override fun loadThemeMode(): ThemeMode {
    return themeMode.value
  }

  override fun loadUseDynamicColors(): Boolean {
    return useDynamicColors.value
  }

  override fun loadThemeTone(): ThemeTone {
    return themeTone.value
  }

  override fun setThemeModeState(mode: ThemeMode) {

  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {

  }

  override fun setThemeTone(tone: ThemeTone) {

  }
}

