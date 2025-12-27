package com.alorma.caducity.base.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.compositeOver

@Suppress("ModifierRequired")
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

  val dims = CaducityDims(
    noDim = 1f,
    dim1 = 0.72f,
    dim2 = 0.68f,
    dim3 = 0.40f,
    dim4 = 0.16f,
    dim5 = 0.08f,
  )

  val expirationColorScheme = rememberExpirationColorScheme(
    schemeType = themePreferences.expirationColorSchemeType.value,
    darkTheme = darkTheme,
    colorScheme = colorScheme,
    dims = dims,
  )

  val colors = CaducityColors.fromColorScheme(
    colorScheme = colorScheme,
    expirationColorScheme = expirationColorScheme,
  )

  CompositionLocalProvider(
    LocalCaducityColors provides colors,
    LocalCaducityDims provides dims
  ) {
    MaterialExpressiveTheme(
      colorScheme = LocalCaducityColors.current.colorScheme,
      motionScheme = MotionScheme.expressive(),
      content = content,
    )
  }
}

@Suppress("ContentEmission")
@Composable
private fun rememberExpirationColorScheme(
  schemeType: ExpirationColorSchemeType,
  darkTheme: Boolean,
  colorScheme: ColorScheme,
  dims: CaducityDims,
): ExpirationColorScheme {
  return when (schemeType) {
    ExpirationColorSchemeType.VIBRANT -> {
      if (darkTheme) {
        darkExpirationColorScheme
      } else {
        lightExpirationColorScheme
      }
    }

    ExpirationColorSchemeType.HARMONIZE -> {
      // Hue values: Green ~120°, Orange ~30°, Red ~0°
      val freshColor = colorScheme.primary
        .shiftHueTowards(targetHue = 120f, amount = 1.0f)
        .copy(alpha = dims.dim3)
        .compositeOver(colorScheme.surface)

      val expiringSoonColor = colorScheme.primary
        .shiftHueTowards(targetHue = 30f, amount = 1.0f)
        .copy(alpha = dims.dim2)
        .compositeOver(colorScheme.surface)

      val expiredColor = colorScheme.primary
        .shiftHueTowards(targetHue = 0f, amount = 1.0f)
        .copy(alpha = dims.dim1)
        .compositeOver(colorScheme.surface)

      ExpirationColorScheme(
        fresh = freshColor,
        onFresh = colorScheme.onPrimary,
        expiringSoon = expiringSoonColor,
        onExpiringSoon = colorScheme.onPrimary,
        expired = expiredColor,
        onExpired = colorScheme.onPrimary,
      )
    }

    ExpirationColorSchemeType.GREY -> {
      val freshColor = colorScheme.surfaceContainerLowest
      val expiringSoonColor = colorScheme.surfaceContainerHigh
      val expiredColor = colorScheme.surfaceContainerHighest

      ExpirationColorScheme(
        fresh = freshColor,
        onFresh = colorScheme.onSurface,
        expiringSoon = expiringSoonColor,
        onExpiringSoon = colorScheme.onSurface,
        expired = expiredColor,
        onExpired = colorScheme.onSurface,
      )
    }
  }
}

