package com.alorma.caducity.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Colors d'un estat: `strong` per a barres, punts i formes; mai per a text petit. */
@Immutable
data class StatusColors(
  val strong: Color,
  val container: Color,
  val onContainer: Color,
)

/** Colors que no caben a `ColorScheme`. Els d'estat són semàntics i no depenen del color dinàmic. */
@Immutable
data class CaducityColors(
  val surfaceShape: Color,
  val expired: StatusColors,
  val soon: StatusColors,
  val fresh: StatusColors,
  val frozen: StatusColors,
)

val LightCaducityColors =
  CaducityColors(
    surfaceShape = SurfaceShapeLight,
    expired = StatusColors(ExpiredLight, ExpiredContainerLight, OnExpiredContainerLight),
    soon = StatusColors(SoonLight, SoonContainerLight, OnSoonContainerLight),
    fresh = StatusColors(FreshLight, FreshContainerLight, OnFreshContainerLight),
    frozen = StatusColors(FrozenLight, FrozenContainerLight, OnFrozenContainerLight),
  )

val DarkCaducityColors =
  CaducityColors(
    surfaceShape = SurfaceShapeDark,
    expired = StatusColors(ExpiredDark, ExpiredContainerDark, OnExpiredContainerDark),
    soon = StatusColors(SoonDark, SoonContainerDark, OnSoonContainerDark),
    fresh = StatusColors(FreshDark, FreshContainerDark, OnFreshContainerDark),
    frozen = StatusColors(FrozenDark, FrozenContainerDark, OnFrozenContainerDark),
  )
