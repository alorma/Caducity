package com.alorma.caducity.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CaducityColors(
  val primary: Color,
  val onPrimary: Color,
  val primaryContainer: Color,
  val onPrimaryContainer: Color,
  val inversePrimary: Color,
  val secondary: Color,
  val onSecondary: Color,
  val secondaryContainer: Color,
  val onSecondaryContainer: Color,
  val tertiary: Color,
  val onTertiary: Color,
  val tertiaryContainer: Color,
  val onTertiaryContainer: Color,
  val background: Color,
  val onBackground: Color,
  val surface: Color,
  val onSurface: Color,
  val surfaceVariant: Color,
  val onSurfaceVariant: Color,
  val surfaceTint: Color,
  val inverseSurface: Color,
  val inverseOnSurface: Color,
  val error: Color,
  val onError: Color,
  val errorContainer: Color,
  val onErrorContainer: Color,
  val outline: Color,
  val outlineVariant: Color,
  val scrim: Color,
  val surfaceBright: Color,
  val surfaceDim: Color,
  val surfaceContainer: Color,
  val surfaceContainerHigh: Color,
  val surfaceContainerHighest: Color,
  val surfaceContainerLow: Color,
  val surfaceContainerLowest: Color,
  val primaryFixed: Color,
  val primaryFixedDim: Color,
  val onPrimaryFixed: Color,
  val onPrimaryFixedVariant: Color,
  val secondaryFixed: Color,
  val secondaryFixedDim: Color,
  val onSecondaryFixed: Color,
  val onSecondaryFixedVariant: Color,
  val tertiaryFixed: Color,
  val tertiaryFixedDim: Color,
  val onTertiaryFixed: Color,
  val onTertiaryFixedVariant: Color,
) {
  fun asColorScheme() = ColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    inversePrimary = inversePrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    surfaceTint = surfaceTint,
    inverseSurface = inverseSurface,
    inverseOnSurface = inverseOnSurface,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
    outline = outline,
    outlineVariant = outlineVariant,
    scrim = scrim,
    surfaceBright = surfaceBright,
    surfaceDim = surfaceDim,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    surfaceContainerHighest = surfaceContainerHighest,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainerLowest = surfaceContainerLowest,
    primaryFixed = primaryFixed,
    primaryFixedDim = primaryFixedDim,
    onPrimaryFixed = onPrimaryFixed,
    onPrimaryFixedVariant = onPrimaryFixedVariant,
    secondaryFixed = secondaryFixed,
    secondaryFixedDim = secondaryFixedDim,
    onSecondaryFixed = onSecondaryFixed,
    onSecondaryFixedVariant = onSecondaryFixedVariant,
    tertiaryFixed = tertiaryFixed,
    tertiaryFixedDim = tertiaryFixedDim,
    onTertiaryFixed = onTertiaryFixed,
    onTertiaryFixedVariant = onTertiaryFixedVariant,
  )

  companion object {
    fun fromColorScheme(colorScheme: ColorScheme): CaducityColors {
      return CaducityColors(
        primary = colorScheme.primary,
        onPrimary = colorScheme.onPrimary,
        primaryContainer = colorScheme.primaryContainer,
        onPrimaryContainer = colorScheme.onPrimaryContainer,
        inversePrimary = colorScheme.inversePrimary,
        secondary = colorScheme.secondary,
        onSecondary = colorScheme.onSecondary,
        secondaryContainer = colorScheme.secondaryContainer,
        onSecondaryContainer = colorScheme.onSecondaryContainer,
        tertiary = colorScheme.tertiary,
        onTertiary = colorScheme.onTertiary,
        tertiaryContainer = colorScheme.tertiaryContainer,
        onTertiaryContainer = colorScheme.onTertiaryContainer,
        background = colorScheme.background,
        onBackground = colorScheme.onBackground,
        surface = colorScheme.surface,
        onSurface = colorScheme.onSurface,
        surfaceVariant = colorScheme.surfaceVariant,
        onSurfaceVariant = colorScheme.onSurfaceVariant,
        surfaceTint = colorScheme.surfaceTint,
        inverseSurface = colorScheme.inverseSurface,
        inverseOnSurface = colorScheme.inverseOnSurface,
        error = colorScheme.error,
        onError = colorScheme.onError,
        errorContainer = colorScheme.errorContainer,
        onErrorContainer = colorScheme.onErrorContainer,
        outline = colorScheme.outline,
        outlineVariant = colorScheme.outlineVariant,
        scrim = colorScheme.scrim,
        surfaceBright = colorScheme.surfaceBright,
        surfaceDim = colorScheme.surfaceDim,
        surfaceContainer = colorScheme.surfaceContainer,
        surfaceContainerHigh = colorScheme.surfaceContainerHigh,
        surfaceContainerHighest = colorScheme.surfaceContainerHighest,
        surfaceContainerLow = colorScheme.surfaceContainerLow,
        surfaceContainerLowest = colorScheme.surfaceContainerLowest,
        primaryFixed = colorScheme.primaryFixed,
        primaryFixedDim = colorScheme.primaryFixedDim,
        onPrimaryFixed = colorScheme.onPrimaryFixed,
        onPrimaryFixedVariant = colorScheme.onPrimaryFixedVariant,
        secondaryFixed = colorScheme.secondaryFixed,
        secondaryFixedDim = colorScheme.secondaryFixedDim,
        onSecondaryFixed = colorScheme.onSecondaryFixed,
        onSecondaryFixedVariant = colorScheme.onSecondaryFixedVariant,
        tertiaryFixed = colorScheme.tertiaryFixed,
        tertiaryFixedDim = colorScheme.tertiaryFixedDim,
        onTertiaryFixed = colorScheme.onTertiaryFixed,
        onTertiaryFixedVariant = colorScheme.onTertiaryFixedVariant,
      )
    }
  }
}

internal val LocalCaducityColors = staticCompositionLocalOf<CaducityColors> {
  error("No CaducityThemeColors defined")
}

object CaducityTheme {

  val colors: CaducityColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCaducityColors.current

  val shapes: Shapes
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes

  val typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.typography

}