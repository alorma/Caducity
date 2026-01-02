package com.alorma.caducity.ui.theme

import androidx.compose.runtime.MutableState
import com.alorma.caducity.ui.theme.colors.ExpirationColorSchemeType

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