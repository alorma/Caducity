package com.alorma.caducity.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
  themePreferences: ThemePreferences = koinInject<ThemePreferences>(),
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

  MaterialExpressiveTheme(
    colorScheme = colorScheme,
    motionScheme = MotionScheme.expressive(),
    content = content,
  )
}
