package com.alorma.caducity.ui.theme.colors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.CaducityTheme

data class ExpirationColorScheme(
  val baseColor: Color,

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

class BaseExpirationColors {
  val freshColor: Color = Color.Green
  val expiringSoonColor: Color = Color(0xFFFFDE21)
  val expiredColor: Color = Color.Red
  val frozenColor: Color = Color.Cyan
  val consumedColor: Color = Color.Gray

  @get:ReadOnlyComposable
  @get:Composable
  val baseColor: Color
    get() = CaducityTheme.colorScheme.surfaceContainerHighest

}
