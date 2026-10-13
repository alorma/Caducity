package com.alorma.caducity.ui.theme.colors.palettes

import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.colors.PaletteTokens

internal object BessaPalette {
  val light =
    PaletteTokens(
      surface = Color(0xFFFFF5F8),
      surfaceCard = Color(0xFFF9E7EE),
      surfaceHigh = Color(0xFFF1DAE4),
      surfaceShape = Color(0xFFEACDDA),
      ink = Color(0xFF2E1823),
      inkVariant = Color(0xFF6F4A5B),
      outline = Color(0xFF9B7686),
      outlineVariant = Color(0xFFE4CDD7),
      primary = Color(0xFF8A2E5C),
      onPrimary = Color(0xFFFFFFFF),
      primaryContainer = Color(0xFFF5D5E4),
      onPrimaryContainer = Color(0xFF3A0A22),
    )

  val dark =
    PaletteTokens(
      surface = Color(0xFF1C1318),
      surfaceCard = Color(0xFF281B22),
      surfaceHigh = Color(0xFF33242C),
      surfaceShape = Color(0xFF402E38),
      ink = Color(0xFFF4E4EB),
      inkVariant = Color(0xFFCDB1BE),
      outline = Color(0xFF9B8090),
      outlineVariant = Color(0xFF52404A),
      primary = Color(0xFFF0A2C4),
      onPrimary = Color(0xFF4A0A2A),
      primaryContainer = Color(0xFF6B1F46),
      onPrimaryContainer = Color(0xFFF5D5E4),
    )
}
