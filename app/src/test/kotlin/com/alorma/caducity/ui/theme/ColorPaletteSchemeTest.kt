package com.alorma.caducity.ui.theme

import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.colors.DarkStateColors
import com.alorma.caducity.ui.theme.colors.LightStateColors
import com.alorma.caducity.ui.theme.colors.accent
import com.alorma.caducity.ui.theme.colors.scheme
import com.alorma.caducity.ui.theme.colors.tokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorPaletteSchemeTest {
  private val fixed = ColorPalette.entries.filter { it != ColorPalette.DYNAMIC }

  @Test
  fun `every palette gives an opaque scheme in light and dark`() {
    for (palette in ColorPalette.entries) {
      for (dark in listOf(false, true)) {
        val s = palette.scheme(dark)
        listOf(s.primary, s.onPrimary, s.primaryContainer, s.onPrimaryContainer, s.surface, s.onSurface, s.outline)
          .forEach { assertTrue("$palette dark=$dark", it != Color.Unspecified && it.alpha == 1f) }
      }
    }
  }

  @Test
  fun `palettes differ in ambient tokens`() {
    for (dark in listOf(false, true)) {
      assertEquals(fixed.size, fixed.map { it.scheme(dark).surface }.toSet().size)
      assertEquals(fixed.size, fixed.map { it.scheme(dark).primary }.toSet().size)
    }
  }

  @Test
  fun `state roles do not depend on the palette`() {
    for (dark in listOf(false, true)) {
      val reference = ColorPalette.REBOST.scheme(dark)
      for (palette in ColorPalette.entries) {
        val s = palette.scheme(dark)
        assertEquals(reference.error, s.error)
        assertEquals(reference.errorContainer, s.errorContainer)
        assertEquals(reference.tertiary, s.tertiary)
        assertEquals(reference.tertiaryContainer, s.tertiaryContainer)
      }
    }
  }

  @Test
  fun `error matches the expired state color`() {
    assertEquals(LightStateColors.expired.strong, ColorPalette.NEVERA.scheme(false).error)
    assertEquals(DarkStateColors.expired.strong, ColorPalette.NEVERA.scheme(true).error)
  }

  @Test
  fun `dynamic falls back to Rebost tokens`() {
    assertEquals(ColorPalette.REBOST.tokens(false), ColorPalette.DYNAMIC.tokens(false))
    assertEquals(ColorPalette.REBOST.tokens(true), ColorPalette.DYNAMIC.tokens(true))
  }

  @Test
  fun `light and dark differ`() {
    for (palette in fixed) assertNotEquals(palette.scheme(false).surface, palette.scheme(true).surface)
  }

  @Test
  fun `notification accent follows the palette`() {
    assertEquals(fixed.size, fixed.map { it.accent() }.toSet().size)
    assertEquals(ColorPalette.NEVERA.tokens(false).primary, ColorPalette.NEVERA.accent())
    assertEquals(ColorPalette.REBOST.accent(), ColorPalette.DYNAMIC.accent())
  }
}
