package com.alorma.caducity.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.alorma.caducity.ui.theme.colors.DarkStateColors
import com.alorma.caducity.ui.theme.colors.ExpiredDark
import com.alorma.caducity.ui.theme.colors.ExpiredLight
import com.alorma.caducity.ui.theme.colors.LightStateColors
import com.alorma.caducity.ui.theme.colors.OnExpiredDark
import com.alorma.caducity.ui.theme.colors.OnExpiredLight
import com.alorma.caducity.ui.theme.colors.StateColors
import com.alorma.caducity.ui.theme.colors.tokens
import org.junit.Test

/** WCAG 2.1 sobre les paletes de Kotlin: mínims de l'especificació (clar / fosc) i estats ≥ 4,5:1. */
class PaletteContrastTest {
  private companion object {
    /** L'especificació dona els mínims arrodonits a un decimal. */
    const val ROUNDING = 0.05
  }

  private val fixed = ColorPalette.entries.filter { it != ColorPalette.DYNAMIC }

  private fun contrast(
    a: Color,
    b: Color,
  ): Double {
    val la = a.luminance().toDouble()
    val lb = b.luminance().toDouble()
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
  }

  private fun check(
    label: String,
    fg: Color,
    bg: Color,
    min: Double,
  ) {
    val ratio = contrast(fg, bg)
    check(ratio >= min - ROUNDING) { "$label = ${"%.2f".format(ratio)} (min $min)" }
  }

  private fun forEachPaletteAndTheme(block: (ColorPalette, Boolean) -> Unit) {
    for (palette in fixed) for (dark in listOf(false, true)) block(palette, dark)
  }

  private fun label(
    palette: ColorPalette,
    dark: Boolean,
    what: String,
  ) = "$palette ${if (dark) "dark" else "light"}: $what"

  @Test
  fun `text on background`() =
    forEachPaletteAndTheme { p, dark ->
      val t = p.tokens(dark)
      check(label(p, dark, "ink on surface"), t.ink, t.surface, if (dark) 14.4 else 13.6)
    }

  @Test
  fun `ink variant on card`() =
    forEachPaletteAndTheme { p, dark ->
      val t = p.tokens(dark)
      check(label(p, dark, "ink-variant on surface-card"), t.inkVariant, t.surfaceCard, if (dark) 8.0 else 5.4)
    }

  @Test
  fun `on primary on primary`() =
    forEachPaletteAndTheme { p, dark ->
      val t = p.tokens(dark)
      check(label(p, dark, "on-primary on primary"), t.onPrimary, t.primary, if (dark) 7.5 else 5.2)
    }

  @Test
  fun `content on primary container`() =
    forEachPaletteAndTheme { p, dark ->
      val t = p.tokens(dark)
      check(
        label(p, dark, "on-primary-container on primary-container"),
        t.onPrimaryContainer,
        t.primaryContainer,
        if (dark) 6.8 else 10.4,
      )
    }

  @Test
  fun `ink on card meets AA`() =
    forEachPaletteAndTheme { p, dark ->
      val t = p.tokens(dark)
      check(label(p, dark, "ink on surface-card"), t.ink, t.surfaceCard, 4.5)
    }

  @Test
  fun `state pairs meet AA in light and dark`() {
    fun states(
      name: String,
      s: StateColors,
      onExpired: Color,
      expired: Color,
    ) {
      check("$name on-expired on expired", onExpired, expired, 4.5)
      listOf("expired" to s.expired, "soon" to s.soon, "fresh" to s.fresh, "frozen" to s.frozen)
        .forEach { (n, c) -> check("$name on-$n-container on $n-container", c.onContainer, c.container, 4.5) }
    }
    states("light", LightStateColors, OnExpiredLight, ExpiredLight)
    states("dark", DarkStateColors, OnExpiredDark, ExpiredDark)
  }
}
