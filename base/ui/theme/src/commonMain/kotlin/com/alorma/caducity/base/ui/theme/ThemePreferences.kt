package com.alorma.caducity.base.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

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

object ThemePreferencesNoOp : ThemePreferences {
  override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
  override val useDynamicColors: MutableState<Boolean> = mutableStateOf(true)
  override val expirationColorSchemeType: MutableState<ExpirationColorSchemeType> = mutableStateOf(ExpirationColorSchemeType.VIBRANT)

  override fun loadThemeMode(): ThemeMode {
    return ThemeMode.SYSTEM
  }

  override fun loadUseDynamicColors(): Boolean {
    return true
  }

  override fun loadExpirationColorSchemeType(): ExpirationColorSchemeType {
    return ExpirationColorSchemeType.VIBRANT
  }

  override fun setThemeModeState(mode: ThemeMode) {

  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {

  }

  override fun setExpirationColorSchemeType(type: ExpirationColorSchemeType) {

  }

}