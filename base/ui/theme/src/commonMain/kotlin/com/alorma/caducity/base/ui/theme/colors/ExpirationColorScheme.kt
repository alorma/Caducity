package com.alorma.caducity.base.ui.theme.colors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.base.ui.theme.CaducityTheme

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

interface BaseExpirationColors {
  val freshColor: Color
  val expiringSoonColor: Color
  val expiredColor: Color
  val frozenColor: Color
  val consumedColor: Color

  @get:ReadOnlyComposable
  @get:Composable
  val baseColor: Color
}

class CaducityBaseExpirationColors : BaseExpirationColors {
  override val freshColor: Color = Color.Green
  override val expiringSoonColor: Color = Color(0xFFFFDE21)
  override val expiredColor: Color = Color.Red
  override val frozenColor: Color = Color.Cyan
  override val consumedColor: Color = Color.Gray

  @get:ReadOnlyComposable
  @get:Composable
  override val baseColor: Color
    get() = CaducityTheme.colorScheme.surfaceContainerHighest

}
