package com.alorma.caducity.ui.theme

import android.app.UiModeManager
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.getSystemService

/**
 * Android platform color schemes.
 * Uses the base light and dark schemes from Color.kt
 */
@Composable
actual fun platformLightColorScheme(): ColorScheme {
  return lightScheme
}

@Composable
actual fun platformDarkColorScheme(): ColorScheme = darkScheme

@Composable
fun selectSchemeForContrast(isDark: Boolean): ColorScheme {
  val context = LocalContext.current
  var colorScheme = if (isDark) darkScheme else lightScheme
  if (!LocalInspectionMode.current) {
    val uiModeManager = context.getSystemService<UiModeManager>()
    val contrastLevel = uiModeManager?.contrast ?: 0f

    colorScheme = when (contrastLevel) {
      in 0.0f..0.33f -> if (isDark) {
        darkScheme
      } else {
        lightScheme
      }

      in 0.34f..0.66f -> if (isDark) {
        mediumContrastDarkColorScheme
      } else {
        mediumContrastLightColorScheme
      }

      in 0.67f..1.0f -> if (isDark) {
        highContrastDarkColorScheme
      } else {
        highContrastLightColorScheme
      }

      else -> if (isDark) {
        darkScheme
      } else {
        lightScheme
      }
    }
    return colorScheme
  } else return colorScheme
}