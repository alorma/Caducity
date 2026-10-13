package com.alorma.caducity.ui.theme.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.alorma.caducity.ui.theme.AppThemeContent
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferences
import com.alorma.caducity.ui.theme.ThemePreferencesNoOp

@Suppress("ModifierRequired")
@Composable
fun PreviewTheme(block: @Composable () -> Unit) {
  AppThemeContent(themePreferences = ThemePreferencesNoOp) { block() }
}

/** Tema amb una paleta concreta, en clar o fosc, per a les previews i captures per paleta. */
@Suppress("ModifierRequired")
@Composable
fun PreviewTheme(
  palette: ColorPalette,
  darkTheme: Boolean,
  block: @Composable () -> Unit,
) {
  val preferences =
    remember(palette, darkTheme) {
      object : ThemePreferences {
        override val themeMode = mutableStateOf(if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT)
        override val colorPalette = mutableStateOf(palette)

        override fun loadThemeMode() = themeMode.value

        override fun loadColorPalette() = colorPalette.value

        override fun setThemeModeState(mode: ThemeMode) = Unit

        override fun setColorPalette(palette: ColorPalette) = Unit
      }
    }
  AppThemeContent(themePreferences = preferences) {
    Surface(color = MaterialTheme.colorScheme.background) { block() }
  }
}
