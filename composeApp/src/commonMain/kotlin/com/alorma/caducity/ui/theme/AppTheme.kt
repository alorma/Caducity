package com.alorma.caducity.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
  themePreferences: ThemePreferences = ThemePreferences(),
  content: @Composable () -> Unit,
) {
  val systemInDarkTheme = isSystemInDarkTheme()
  
  val darkTheme = when (themePreferences.themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> systemInDarkTheme
  }

  val colorScheme: ColorScheme = if (themePreferences.useDynamicColors) {
    dynamicColorScheme(darkTheme) ?: if (darkTheme) darkColorScheme() else lightColorScheme()
  } else {
    if (darkTheme) darkColorScheme() else lightColorScheme()
  }

  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
  )
}
