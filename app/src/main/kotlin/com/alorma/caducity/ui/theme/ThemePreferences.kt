package com.alorma.caducity.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

interface ThemePreferences {
  val themeMode: MutableState<ThemeMode>
  val colorPalette: MutableState<ColorPalette>

  fun loadThemeMode(): ThemeMode

  fun loadColorPalette(): ColorPalette

  fun setThemeModeState(mode: ThemeMode)

  fun setColorPalette(palette: ColorPalette)
}

object ThemePreferencesNoOp : ThemePreferences {
  override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
  override val colorPalette: MutableState<ColorPalette> = mutableStateOf(ColorPalette.DYNAMIC)

  override fun loadThemeMode(): ThemeMode = themeMode.value

  override fun loadColorPalette(): ColorPalette = colorPalette.value

  override fun setThemeModeState(mode: ThemeMode) {
  }

  override fun setColorPalette(palette: ColorPalette) {
  }
}
