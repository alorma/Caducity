package com.alorma.caducity.base.ui.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun dynamicColorScheme(isDark: Boolean): ColorScheme {
  val context = LocalContext.current
  return if (isDark) {
    dynamicDarkColorScheme(context)
  } else {
    dynamicLightColorScheme(context)
  }
}