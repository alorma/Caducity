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

/** Colors d'estat: semàntics, iguals a totes les paletes (i al color dinàmic). */
@Immutable
data class StateColors(
  val expired: StatusColors,
  val soon: StatusColors,
  val fresh: StatusColors,
  val frozen: StatusColors,
)

val LightStateColors =
  StateColors(
    expired = StatusColors(ExpiredLight, ExpiredContainerLight, OnExpiredContainerLight),
    soon = StatusColors(SoonLight, SoonContainerLight, OnSoonContainerLight),
    fresh = StatusColors(FreshLight, FreshContainerLight, OnFreshContainerLight),
    frozen = StatusColors(FrozenLight, FrozenContainerLight, OnFrozenContainerLight),
  )

val DarkStateColors =
  StateColors(
    expired = StatusColors(ExpiredDark, ExpiredContainerDark, OnExpiredContainerDark),
    soon = StatusColors(SoonDark, SoonContainerDark, OnSoonContainerDark),
    fresh = StatusColors(FreshDark, FreshContainerDark, OnFreshContainerDark),
    frozen = StatusColors(FrozenDark, FrozenContainerDark, OnFrozenContainerDark),
  )

/** Colors que no caben a `ColorScheme`: `surfaceShape` de la paleta activa + els estats. */
@Immutable
data class CaducityColors(
  val surfaceShape: Color,
  val expired: StatusColors,
  val soon: StatusColors,
  val fresh: StatusColors,
  val frozen: StatusColors,
) {
  constructor(surfaceShape: Color, states: StateColors) :
    this(surfaceShape, states.expired, states.soon, states.fresh, states.frozen)
}
