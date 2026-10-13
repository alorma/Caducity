package com.alorma.caducity.ui.theme.colors.palettes

import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.colors.PaletteTokens

internal object NeveraPalette {
  val light =
    PaletteTokens(
      surface = Color(0xFFF4F6FB),
      surfaceCard = Color(0xFFE9EDF6),
      surfaceHigh = Color(0xFFDFE5F1),
      surfaceShape = Color(0xFFD5DCEB),
      ink = Color(0xFF1D2433),
      inkVariant = Color(0xFF55607A),
      outline = Color(0xFF7C869C),
      outlineVariant = Color(0xFFCDD4E3),
      primary = Color(0xFF3E5C94),
      onPrimary = Color(0xFFFFFFFF),
      primaryContainer = Color(0xFFD9E2F5),
      onPrimaryContainer = Color(0xFF14264A),
    )

  val dark =
    PaletteTokens(
      surface = Color(0xFF14161E),
      surfaceCard = Color(0xFF1D202B),
      surfaceHigh = Color(0xFF272B38),
      surfaceShape = Color(0xFF323747),
      ink = Color(0xFFE6E9F4),
      inkVariant = Color(0xFFB2B8CC),
      outline = Color(0xFF8A91A8),
      outlineVariant = Color(0xFF3E4457),
      primary = Color(0xFFAFC3F2),
      onPrimary = Color(0xFF11264F),
      primaryContainer = Color(0xFF2A3D68),
      onPrimaryContainer = Color(0xFFD9E2F8),
    )
}
