package com.alorma.caducity.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Els 12 tokens d'ambient que canvien amb la paleta (clar o fosc). */
@Immutable
data class PaletteTokens(
  val surface: Color,
  val surfaceCard: Color,
  val surfaceHigh: Color,
  val surfaceShape: Color,
  val ink: Color,
  val inkVariant: Color,
  val outline: Color,
  val outlineVariant: Color,
  val primary: Color,
  val onPrimary: Color,
  val primaryContainer: Color,
  val onPrimaryContainer: Color,
)
