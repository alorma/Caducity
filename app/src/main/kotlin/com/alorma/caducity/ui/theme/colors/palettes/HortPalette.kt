package com.alorma.caducity.ui.theme.colors.palettes

import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.colors.PaletteTokens

internal object HortPalette {
  val light =
    PaletteTokens(
      surface = Color(0xFFF6FAEC),
      surfaceCard = Color(0xFFEAF3D6),
      surfaceHigh = Color(0xFFDFEBC4),
      surfaceShape = Color(0xFFD2E4AE),
      ink = Color(0xFF1B2814),
      inkVariant = Color(0xFF4C6339),
      outline = Color(0xFF7A9065),
      outlineVariant = Color(0xFFC8DDA8),
      primary = Color(0xFF2E7D1F),
      onPrimary = Color(0xFFFFFFFF),
      primaryContainer = Color(0xFFD3EE9A),
      onPrimaryContainer = Color(0xFF12360A),
    )

  val dark =
    PaletteTokens(
      surface = Color(0xFF13170F),
      surfaceCard = Color(0xFF1C2116),
      surfaceHigh = Color(0xFF262C1E),
      surfaceShape = Color(0xFF313925),
      ink = Color(0xFFE7F0DA),
      inkVariant = Color(0xFFB5C4A0),
      outline = Color(0xFF8A9877),
      outlineVariant = Color(0xFF3C4630),
      primary = Color(0xFF8EDB74),
      onPrimary = Color(0xFF0A3004),
      primaryContainer = Color(0xFF254A18),
      onPrimaryContainer = Color(0xFFD3EE9A),
    )
}
