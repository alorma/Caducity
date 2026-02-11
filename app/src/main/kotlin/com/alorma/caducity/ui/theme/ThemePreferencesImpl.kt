package com.alorma.caducity.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.alorma.caducity.ui.theme.colors.supportsDynamicColors
import com.russhwolf.settings.Settings

class ThemePreferencesImpl(private val settings: Settings) : ThemePreferences {
  companion object {
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_USE_DYNAMIC_COLORS = "use_dynamic_colors"
    private const val KEY_TONAL_COLOR_MODE = "tonal_color_mode"
  }

  override val themeMode = mutableStateOf(loadThemeMode())
  override val useDynamicColors = mutableStateOf(loadUseDynamicColors())
  override val tonalColorMode = mutableStateOf(loadTonalColorMode())

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
    return settings.getBoolean(KEY_USE_DYNAMIC_COLORS, false)
  }

  override fun loadTonalColorMode(): TonalColorMode {
    val savedValue = settings.getStringOrNull(KEY_TONAL_COLOR_MODE)
    return savedValue?.let {
      try {
        TonalColorMode.valueOf(it)
      } catch (_: IllegalArgumentException) {
        TonalColorMode.VIBRANT
      }
    } ?: TonalColorMode.VIBRANT
  }

  override fun setThemeModeState(mode: ThemeMode) {
    themeMode.value = mode
    settings.putString(KEY_THEME_MODE, mode.name)
  }

  override fun setDynamicColorsEnabled(enabled: Boolean) {
    useDynamicColors.value = enabled
    settings.putBoolean(KEY_USE_DYNAMIC_COLORS, enabled)
  }

  override fun setTonalColorMode(mode: TonalColorMode) {
    tonalColorMode.value = mode
    settings.putString(KEY_TONAL_COLOR_MODE, mode.name)
  }
}
