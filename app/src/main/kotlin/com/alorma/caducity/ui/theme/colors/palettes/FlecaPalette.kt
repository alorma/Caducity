package com.alorma.caducity.ui.theme.colors.palettes

import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.colors.PaletteTokens

internal object FlecaPalette {
  val light =
    PaletteTokens(
      surface = Color(0xFFFFF4EA),
      surfaceCard = Color(0xFFF9E8D8),
      surfaceHigh = Color(0xFFF1DBC6),
      surfaceShape = Color(0xFFEAD0B6),
      ink = Color(0xFF3A2216),
      inkVariant = Color(0xFF7A5644),
      outline = Color(0xFFA07C68),
      outlineVariant = Color(0xFFE6CDBA),
      primary = Color(0xFF8A4B12),
      onPrimary = Color(0xFFFFFFFF),
      primaryContainer = Color(0xFFF6DAB5),
      onPrimaryContainer = Color(0xFF3B1F02),
    )

  val dark =
    PaletteTokens(
      surface = Color(0xFF1E1610),
      surfaceCard = Color(0xFF2A1F17),
      surfaceHigh = Color(0xFF352920),
      surfaceShape = Color(0xFF42342A),
      ink = Color(0xFFF6E8DA),
      inkVariant = Color(0xFFCDB6A2),
      outline = Color(0xFF9E8470),
      outlineVariant = Color(0xFF54433A),
      primary = Color(0xFFF0B77C),
      onPrimary = Color(0xFF4A2600),
      primaryContainer = Color(0xFF6A3A0E),
      onPrimaryContainer = Color(0xFFF6DAB5),
    )
}
