package com.alorma.caducity.ui.screen.transform

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** The least contrast a border or a line needs against the background to be seen (WCAG non-text contrast). */
internal const val MinLineContrast = 3f

private const val DarkenStep = 0.05f

/** WCAG contrast ratio between two opaque colours. */
internal fun contrastRatio(
  a: Color,
  b: Color,
): Float {
  val la = a.luminance()
  val lb = b.luminance()
  return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

/**
 * This colour, darkened towards black (or lightened towards white on a dark background) until it reaches
 * [MinLineContrast] against [background]. A colour that already does is returned as it is.
 */
internal fun Color.withLineContrast(background: Color): Color {
  val flat = compositeOver(background)
  if (contrastRatio(flat, background) >= MinLineContrast) return this
  val target = if (background.luminance() > 0.5f) Color.Black else Color.White
  var amount = 0f
  var result = flat
  while (amount < 1f && contrastRatio(result, background) < MinLineContrast) {
    amount += DarkenStep
    result = lerp(flat, target, amount)
  }
  return result
}
