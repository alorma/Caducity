package com.alorma.caducity.ui.theme

import kotlin.math.pow
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isGreaterThanOrEqualTo

/** WCAG 2.1: parells de colors de `tokens.json` (clar i fosc) amb contrast ≥ 4,5:1. */
class TokenContrastTest {
  private val tokens: Map<String, Pair<String, String>> by lazy {
    val json = javaClass.getResource("/tokens.json")!!.readText()
    Regex("""\{"name": "([a-z-]+)", "value": \{"light": "(#[0-9A-Fa-f]{6})", "dark": "(#[0-9A-Fa-f]{6})"}""")
      .findAll(json)
      .associate { it.groupValues[1] to (it.groupValues[2] to it.groupValues[3]) }
  }

  private val pairs =
    listOf(
      "ink" to "surface",
      "ink" to "surface-card",
      "ink-variant" to "surface-card",
      "on-primary" to "primary",
      "on-primary-container" to "primary-container",
      "on-expired" to "expired",
      "on-expired-container" to "expired-container",
      "on-soon-container" to "soon-container",
      "on-fresh-container" to "fresh-container",
      "on-frozen-container" to "frozen-container",
    )

  @Test
  fun `tokens parsed`() {
    expectThat(tokens.size).isGreaterThanOrEqualTo(24)
  }

  @Test
  fun `foreground and background pairs meet 4_5 contrast in light and dark`() {
    pairs.forEach { (fg, bg) ->
      listOf(0, 1).forEach { theme ->
        fun pick(n: String) = tokens.getValue(n).let { if (theme == 0) it.first else it.second }
        val ratio = contrast(pick(fg), pick(bg))
        check(ratio >= 4.5) { "$fg on $bg (${if (theme == 0) "light" else "dark"}) = $ratio" }
      }
    }
  }

  private fun luminance(hex: String): Double {
    fun channel(i: Int): Double {
      val c = hex.substring(1 + i * 2, 3 + i * 2).toInt(16) / 255.0
      return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(0) + 0.7152 * channel(1) + 0.0722 * channel(2)
  }

  private fun contrast(
    a: String,
    b: String,
  ): Double {
    val (hi, lo) = luminance(a).let { la -> luminance(b).let { lb -> maxOf(la, lb) to minOf(la, lb) } }
    return (hi + 0.05) / (lo + 0.05)
  }
}
