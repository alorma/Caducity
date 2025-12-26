package com.alorma.caducity.base.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme(
  themePreferences: ThemePreferences = ThemePreferencesNoOp,
  content: @Composable () -> Unit,
) {
  val systemInDarkTheme = isSystemInDarkTheme()

  val darkTheme = when (themePreferences.themeMode.value) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> systemInDarkTheme
  }

  val defaultColorScheme = if (darkTheme) {
    platformDarkColorScheme()
  } else {
    platformLightColorScheme()
  }

  val colorScheme: ColorScheme = if (themePreferences.useDynamicColors.value) {
    dynamicColorScheme(darkTheme) ?: defaultColorScheme
  } else {
    defaultColorScheme
  }

  val colors = CaducityColors.fromColorScheme(
    colorScheme = colorScheme,
  )

  CompositionLocalProvider(
    LocalCaducityColors provides colors
  ) {
    MaterialExpressiveTheme(
      colorScheme = LocalCaducityColors.current.colorScheme,
      motionScheme = MotionScheme.expressive(),
      content = content,
    )
  }
}
