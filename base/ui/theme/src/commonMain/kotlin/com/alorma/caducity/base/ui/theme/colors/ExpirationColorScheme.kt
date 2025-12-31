package com.alorma.caducity.base.ui.theme.colors

import androidx.compose.ui.graphics.Color

data class ExpirationColorScheme(
  val fresh: Color,
  val onFresh: Color,

  val expiringSoon: Color,
  val onExpiringSoon: Color,

  val expired: Color,
  val onExpired: Color,

  val frozen: Color,
  val onFrozen: Color,

  val consumed: Color,
  val onConsumed: Color
)