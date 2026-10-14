package com.alorma.caducity.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ContentOnTest {
  private val ink = Color(0xFF29241C)
  private val cream = Color(0xFFFBF6EC)

  private val light = lightColorScheme(onSurface = ink, surface = cream)
  private val dark = darkColorScheme(onSurface = cream, surface = ink)

  private val darkBackground = Color(0xFF3B2A1E)
  private val lightBackground = Color(0xFFF2D9A0)

  @Test
  fun `in the light scheme a dark background gets the surface`() {
    expectThat(light.contentOn(darkBackground)).isEqualTo(light.surface)
  }

  @Test
  fun `in the light scheme a light background gets the onSurface`() {
    expectThat(light.contentOn(lightBackground)).isEqualTo(light.onSurface)
  }

  @Test
  fun `in the dark scheme a dark background gets the onSurface`() {
    expectThat(dark.contentOn(darkBackground)).isEqualTo(dark.onSurface)
  }

  @Test
  fun `in the dark scheme a light background gets the surface`() {
    expectThat(dark.contentOn(lightBackground)).isEqualTo(dark.surface)
  }
}
