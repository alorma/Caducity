package com.alorma.caducity.base.ui.theme

import androidx.compose.runtime.MutableState

interface ThemePreferences {
  val themeMode: MutableState<ThemeMode>
  val useDynamicColors: MutableState<Boolean>
  val expirationColorSchemeType: MutableState<ExpirationColorSchemeType>

  fun loadThemeMode(): ThemeMode
  fun loadUseDynamicColors(): Boolean
  fun loadExpirationColorSchemeType(): ExpirationColorSchemeType
  fun setThemeModeState(mode: ThemeMode)
  fun setDynamicColorsEnabled(enabled: Boolean)
  fun setExpirationColorSchemeType(type: ExpirationColorSchemeType)
}