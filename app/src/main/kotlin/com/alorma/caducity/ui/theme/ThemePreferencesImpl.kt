package com.alorma.caducity.ui.theme

import androidx.compose.runtime.mutableStateOf
import com.russhwolf.settings.Settings

class ThemePreferencesImpl(
  private val settings: Settings,
) : ThemePreferences {
  companion object {
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_COLOR_PALETTE = "color_palette"
    private const val KEY_USE_DYNAMIC_COLORS = "use_dynamic_colors"
  }

  override val themeMode = mutableStateOf(loadThemeMode())
  override val colorPalette = mutableStateOf(loadColorPalette())

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

  override fun loadColorPalette(): ColorPalette {
    val saved = settings.getStringOrNull(KEY_COLOR_PALETTE)
    if (saved != null) return ColorPalette.fromName(saved)

    // Migration from the old "dynamic colors" switch.
    val migrated =
      if (settings.getBoolean(KEY_USE_DYNAMIC_COLORS, false)) ColorPalette.DYNAMIC else ColorPalette.REBOST
    settings.putString(KEY_COLOR_PALETTE, migrated.name)
    settings.remove(KEY_USE_DYNAMIC_COLORS)
    return migrated
  }

  override fun setThemeModeState(mode: ThemeMode) {
    themeMode.value = mode
    settings.putString(KEY_THEME_MODE, mode.name)
  }

  override fun setColorPalette(palette: ColorPalette) {
    colorPalette.value = palette
    settings.putString(KEY_COLOR_PALETTE, palette.name)
  }
}
