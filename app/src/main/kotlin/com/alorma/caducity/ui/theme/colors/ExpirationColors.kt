package com.alorma.caducity.ui.theme.colors

import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.harmonize

data class ExpirationColors(
  val vibrant: ExpirationColorsPalette,
  val soft: ExpirationColorsPalette,
)

interface ExpirationColorsPalette {
  val fresh: Color
  val expiringSoon: Color
  val expired: Color
  val frozen: Color
  val consumed: Color
}

class DefaultExpirationColors(
  val error: Color,
) : ExpirationColorsPalette {
  override val fresh: Color = Color.Green
  override val expiringSoon: Color = Color(0xFFFFDE21)
  override val expired: Color = error
  override val frozen: Color = Color.Cyan
  override val consumed: Color = Color.Green
}

class VibrantExpirationColors(
  default: ExpirationColorsPalette,
  baseColor: Color,
) : ExpirationColorsPalette {

  override val fresh: Color = default.fresh.harmonize(
    other = baseColor,
    matchSaturation = false,
  )

  override val expiringSoon: Color = default.expiringSoon.harmonize(
    other = baseColor,
    matchSaturation = false,
  )

  override val expired: Color = default.expired.harmonize(
    other = baseColor,
    matchSaturation = false,
  )

  override val frozen: Color = default.frozen.harmonize(
    other = baseColor,
    matchSaturation = false,
  )

  override val consumed: Color = default.consumed.harmonize(
    other = baseColor,
    matchSaturation = false,
  )
}

class SoftExpirationColors(
  default: ExpirationColorsPalette,
  baseColor: Color,
) : ExpirationColorsPalette {

  override val fresh: Color = default.fresh.harmonize(
    other = baseColor,
    matchSaturation = true,
  )

  override val expiringSoon: Color = default.expiringSoon.harmonize(
    other = baseColor,
    matchSaturation = true,
  )

  override val expired: Color = default.expired.harmonize(
    other = baseColor,
    matchSaturation = true,
  )

  override val frozen: Color = default.frozen.harmonize(
    other = baseColor,
    matchSaturation = true,
  )

  override val consumed: Color = default.consumed.harmonize(
    other = baseColor,
    matchSaturation = true,
  )
}