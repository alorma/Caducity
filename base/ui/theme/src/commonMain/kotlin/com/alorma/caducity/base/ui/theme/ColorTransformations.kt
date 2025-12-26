package com.alorma.caducity.base.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal data class HSL(val hue: Float, val saturation: Float, val lightness: Float, val alpha: Float)

internal fun Color.toHSL(): HSL {
  val r = red
  val g = green
  val b = blue

  val max = max(r, max(g, b))
  val min = min(r, min(g, b))
  val delta = max - min

  val lightness = (max + min) / 2f

  val saturation = if (delta == 0f) {
    0f
  } else {
    delta / (1f - abs(2f * lightness - 1f))
  }

  val hue = when {
    delta == 0f -> 0f
    max == r -> 60f * (((g - b) / delta) % 6f)
    max == g -> 60f * (((b - r) / delta) + 2f)
    else -> 60f * (((r - g) / delta) + 4f)
  }.let { if (it < 0f) it + 360f else it }

  return HSL(hue, saturation, lightness, alpha)
}

internal fun HSL.toColor(): Color {
  val c = (1f - abs(2f * lightness - 1f)) * saturation
  val x = c * (1f - abs((hue / 60f) % 2f - 1f))
  val m = lightness - c / 2f

  val (r, g, b) = when {
    hue < 60f -> Triple(c, x, 0f)
    hue < 120f -> Triple(x, c, 0f)
    hue < 180f -> Triple(0f, c, x)
    hue < 240f -> Triple(0f, x, c)
    hue < 300f -> Triple(x, 0f, c)
    else -> Triple(c, 0f, x)
  }

  return Color(r + m, g + m, b + m, alpha)
}

internal fun Color.shiftHueTowards(targetHue: Float, amount: Float = 0.5f): Color {
  val hsl = toHSL()
  val currentHue = hsl.hue

  // Calculate shortest distance between hues (accounting for circular nature of hue)
  val diff = ((targetHue - currentHue + 180f) % 360f) - 180f
  val newHue = (currentHue + diff * amount + 360f) % 360f

  return hsl.copy(hue = newHue).toColor()
}
