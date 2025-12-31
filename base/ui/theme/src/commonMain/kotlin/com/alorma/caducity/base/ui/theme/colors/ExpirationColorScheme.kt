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

val lightExpirationColorScheme = ExpirationColorScheme(
  fresh = Color(0xFF7D9A59),
  onFresh = Color(0xFF000000),

  expiringSoon = Color(0xFFF9DA80),
  onExpiringSoon = Color(0xFF000000),

  expired = Color(0xFFFF5449),
  onExpired = Color(0xFF000000),

  frozen = Color(0xFF81C9FA),
  onFrozen = Color(0xFF000000),

  consumed = Color(0xFF9E9E9E),
  onConsumed = Color(0xFF000000),
)

val darkExpirationColorScheme = ExpirationColorScheme(
  fresh = Color(0xFFB1D18A),
  onFresh = Color(0xFF1F3701),

  expiringSoon = Color(0xFFE1C46D),
  onExpiringSoon = Color(0xFF3C2F00),

  expired = Color(0xFFFFAEA4),
  onExpired = Color(0xFF220001),

  frozen = Color(0xFF9EDAFF),
  onFrozen = Color(0xFF003548),

  consumed = Color(0xFFBDBDBD),
  onConsumed = Color(0xFF303030),
)