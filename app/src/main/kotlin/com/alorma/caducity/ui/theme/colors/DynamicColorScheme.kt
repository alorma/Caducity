package com.alorma.caducity.ui.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.alorma.caducity.ui.theme.ColorPalette

/** Color dinàmic: només substitueix superfícies i `primary*`; la resta del tema (estats, error) no canvia. */
@Composable
fun dynamicColorScheme(isDark: Boolean): ColorScheme {
  val context = LocalContext.current
  val base = ColorPalette.REBOST.scheme(isDark)
  val dynamic = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
  return base.copy(
    primary = dynamic.primary,
    onPrimary = dynamic.onPrimary,
    primaryContainer = dynamic.primaryContainer,
    onPrimaryContainer = dynamic.onPrimaryContainer,
    inversePrimary = dynamic.inversePrimary,
    surfaceTint = dynamic.surfaceTint,
    background = dynamic.background,
    onBackground = dynamic.onBackground,
    surface = dynamic.surface,
    onSurface = dynamic.onSurface,
    surfaceVariant = dynamic.surfaceVariant,
    onSurfaceVariant = dynamic.onSurfaceVariant,
    surfaceBright = dynamic.surfaceBright,
    surfaceDim = dynamic.surfaceDim,
    surfaceContainerLowest = dynamic.surfaceContainerLowest,
    surfaceContainerLow = dynamic.surfaceContainerLow,
    surfaceContainer = dynamic.surfaceContainer,
    surfaceContainerHigh = dynamic.surfaceContainerHigh,
    surfaceContainerHighest = dynamic.surfaceContainerHighest,
  )
}
