package com.alorma.caducity.ui.theme.colors

import androidx.compose.ui.graphics.Color

interface ExpirationColors {
  val fresh: Color
  val expiringSoon: Color
  val expired: Color
  val frozen: Color
  val consumed: Color
}

class DefaultExpirationColors : ExpirationColors {
  override val fresh: Color = Color.Green
  override val expiringSoon: Color = Color(0xFFFFDE21)
  override val expired: Color = Color.Red
  override val frozen: Color = Color.Cyan
  override val consumed: Color = Color.Green
}