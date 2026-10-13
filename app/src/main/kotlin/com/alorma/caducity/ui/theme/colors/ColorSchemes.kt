package com.alorma.caducity.ui.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.colors.palettes.BessaPalette
import com.alorma.caducity.ui.theme.colors.palettes.CampPalette
import com.alorma.caducity.ui.theme.colors.palettes.FlecaPalette
import com.alorma.caducity.ui.theme.colors.palettes.HortPalette
import com.alorma.caducity.ui.theme.colors.palettes.NeveraPalette
import com.alorma.caducity.ui.theme.colors.palettes.RebostPalette

/** Tokens d'una paleta. `DYNAMIC` no en té (ve del sistema): cau al Rebost. */
fun ColorPalette.tokens(darkTheme: Boolean): PaletteTokens =
  when (this) {
    ColorPalette.DYNAMIC, ColorPalette.REBOST -> if (darkTheme) RebostPalette.dark else RebostPalette.light
    ColorPalette.NEVERA -> if (darkTheme) NeveraPalette.dark else NeveraPalette.light
    ColorPalette.HORT -> if (darkTheme) HortPalette.dark else HortPalette.light
    ColorPalette.FLECA -> if (darkTheme) FlecaPalette.dark else FlecaPalette.light
    ColorPalette.BESSA -> if (darkTheme) BessaPalette.dark else BessaPalette.light
    ColorPalette.CAMP -> if (darkTheme) CampPalette.dark else CampPalette.light
  }

/** `ColorScheme` de la paleta; els rols d'estat (`tertiary`, `error`) són els mateixos a totes. */
fun ColorPalette.scheme(darkTheme: Boolean): ColorScheme =
  tokens(darkTheme).toColorScheme(darkTheme, inversePrimary = tokens(!darkTheme).primary)

// Mapatge token → rol M3 (historial de git: docs/redisseny/04-especificacio/03-design-system-compose.md)
fun PaletteTokens.toColorScheme(
  darkTheme: Boolean,
  inversePrimary: Color,
): ColorScheme =
  if (darkTheme) {
    darkColorScheme(
      primary = primary,
      onPrimary = onPrimary,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      inversePrimary = inversePrimary,
      secondary = primary,
      onSecondary = onPrimary,
      secondaryContainer = primaryContainer,
      onSecondaryContainer = onPrimaryContainer,
      tertiary = FrozenDark,
      onTertiary = FrozenContainerDark,
      tertiaryContainer = FrozenContainerDark,
      onTertiaryContainer = OnFrozenContainerDark,
      background = surface,
      onBackground = ink,
      surface = surface,
      onSurface = ink,
      surfaceVariant = surfaceHigh,
      onSurfaceVariant = inkVariant,
      surfaceTint = primary,
      inverseSurface = ink,
      inverseOnSurface = surface,
      error = ExpiredDark,
      onError = OnExpiredDark,
      errorContainer = ExpiredContainerDark,
      onErrorContainer = OnExpiredContainerDark,
      outline = outline,
      outlineVariant = outlineVariant,
      scrim = ScrimDark,
      surfaceBright = surfaceHigh,
      surfaceDim = surface,
      surfaceContainerLowest = surface,
      surfaceContainerLow = surface,
      surfaceContainer = surfaceCard,
      surfaceContainerHigh = surfaceHigh,
      surfaceContainerHighest = surfaceShape,
    )
  } else {
    lightColorScheme(
      primary = primary,
      onPrimary = onPrimary,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      inversePrimary = inversePrimary,
      secondary = primary,
      onSecondary = onPrimary,
      secondaryContainer = primaryContainer,
      onSecondaryContainer = onPrimaryContainer,
      tertiary = FrozenLight,
      onTertiary = Color.White,
      tertiaryContainer = FrozenContainerLight,
      onTertiaryContainer = OnFrozenContainerLight,
      background = surface,
      onBackground = ink,
      surface = surface,
      onSurface = ink,
      surfaceVariant = surfaceHigh,
      onSurfaceVariant = inkVariant,
      surfaceTint = primary,
      inverseSurface = ink,
      inverseOnSurface = surface,
      error = ExpiredLight,
      onError = OnExpiredLight,
      errorContainer = ExpiredContainerLight,
      onErrorContainer = OnExpiredContainerLight,
      outline = outline,
      outlineVariant = outlineVariant,
      scrim = ScrimLight,
      surfaceBright = surface,
      surfaceDim = surfaceHigh,
      surfaceContainerLowest = surface,
      surfaceContainerLow = surface,
      surfaceContainer = surfaceCard,
      surfaceContainerHigh = surfaceHigh,
      surfaceContainerHighest = surfaceShape,
    )
  }
