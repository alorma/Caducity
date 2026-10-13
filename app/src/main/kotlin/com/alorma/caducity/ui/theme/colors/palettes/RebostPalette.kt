package com.alorma.caducity.ui.theme.colors.palettes

import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.colors.PaletteTokens

internal object RebostPalette {
  val light =
    PaletteTokens(
      surface = Color(0xFFFFFAEE),
      surfaceCard = Color(0xFFF7EEDA),
      surfaceHigh = Color(0xFFF0E2CB),
      surfaceShape = Color(0xFFEFD8C0),
      ink = Color(0xFF2B2418),
      inkVariant = Color(0xFF6A5D45),
      outline = Color(0xFF8F8266),
      outlineVariant = Color(0xFFE0D2B2),
      primary = Color(0xFF2E6B3B),
      onPrimary = Color(0xFFFFFFFF),
      primaryContainer = Color(0xFFDCE9C6),
      onPrimaryContainer = Color(0xFF10300F),
    )

  val dark =
    PaletteTokens(
      surface = Color(0xFF1B1A14),
      surfaceCard = Color(0xFF25231B),
      surfaceHigh = Color(0xFF2F2C22),
      surfaceShape = Color(0xFF3A3628),
      ink = Color(0xFFF0E9D6),
      inkVariant = Color(0xFFC2B89C),
      outline = Color(0xFF968C72),
      outlineVariant = Color(0xFF4A4533),
      primary = Color(0xFF8FCB97),
      onPrimary = Color(0xFF0C3014),
      primaryContainer = Color(0xFF1F4A2A),
      onPrimaryContainer = Color(0xFFD5EBCF),
    )
}
