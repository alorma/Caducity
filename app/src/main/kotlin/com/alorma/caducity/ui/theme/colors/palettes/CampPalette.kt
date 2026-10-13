package com.alorma.caducity.ui.theme.colors.palettes

import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.colors.PaletteTokens

internal object CampPalette {
  val light =
    PaletteTokens(
      surface = Color(0xFFFFFBE6),
      surfaceCard = Color(0xFFFAF2CC),
      surfaceHigh = Color(0xFFF3E8B4),
      surfaceShape = Color(0xFFEBDD98),
      ink = Color(0xFF2E2A10),
      inkVariant = Color(0xFF6B6330),
      outline = Color(0xFF948B52),
      outlineVariant = Color(0xFFE5DA9F),
      primary = Color(0xFF6F5A00),
      onPrimary = Color(0xFFFFFFFF),
      primaryContainer = Color(0xFFF7E58A),
      onPrimaryContainer = Color(0xFF3A2F00),
    )

  val dark =
    PaletteTokens(
      surface = Color(0xFF1A1807),
      surfaceCard = Color(0xFF25220E),
      surfaceHigh = Color(0xFF302C14),
      surfaceShape = Color(0xFF3D381B),
      ink = Color(0xFFF2EDC8),
      inkVariant = Color(0xFFC9C195),
      outline = Color(0xFF988F5F),
      outlineVariant = Color(0xFF4B4524),
      primary = Color(0xFFF2D95C),
      onPrimary = Color(0xFF3A3000),
      primaryContainer = Color(0xFF5A4A00),
      onPrimaryContainer = Color(0xFFF7E58A),
    )
}
