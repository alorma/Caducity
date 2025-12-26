package com.alorma.caducity.base.ui.theme

import androidx.compose.runtime.mutableStateOf
import com.russhwolf.settings.Settings

class ThemePreferencesImpl(private val settings: Settings) : ThemePreferences {
  companion object {
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_USE_DYNAMIC_COLORS = "use_dynamic_colors"
  }

  override val themeMode = mutableStateOf(loadThemeMode())
  override val useDynamicColors = mutableStateOf(loadUseDynamicColors())
  
  override fun loadThemeMode(): ThemeMode {
    val savedValue = settings.getStringOrNull(KEY_THEME_MODE)
    return savedValue?.let {
      try {
        ThemeMode.valueOf(it)
      } catch (_: IllegalArgumentException) {
        ThemeMode.SYSTEM
      }
    } ?: ThemeMode.SYSTEM
  }

  override fun loadUseDynamicColors(): Boolean {
    return settings.getBoolean(KEY_USE_DYNAMIC_COLORS, supportsDynamicColors())
  }

  override fun setThemeModeState(mode: ThemeMode) {
    themeMode.value = mode
    settings.putString(KEY_THEME_MODE, mode.name)
  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {
    useDynamicColors.value = enabled
    settings.putBoolean(KEY_USE_DYNAMIC_COLORS, enabled)
  }
}
