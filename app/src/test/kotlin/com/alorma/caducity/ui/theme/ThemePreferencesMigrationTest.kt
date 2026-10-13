package com.alorma.caducity.ui.theme

import com.russhwolf.settings.MapSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ThemePreferencesMigrationTest {
  private fun load(settings: MapSettings) = ThemePreferencesImpl(settings).colorPalette.value

  @Test
  fun `no value defaults to REBOST`() {
    assertEquals(ColorPalette.REBOST, load(MapSettings()))
  }

  @Test
  fun `old dynamic colors enabled migrates to DYNAMIC`() {
    assertEquals(ColorPalette.DYNAMIC, load(MapSettings("use_dynamic_colors" to true)))
  }

  @Test
  fun `old dynamic colors disabled migrates to REBOST`() {
    assertEquals(ColorPalette.REBOST, load(MapSettings("use_dynamic_colors" to false)))
  }

  @Test
  fun `unknown palette falls back to REBOST`() {
    assertEquals(ColorPalette.REBOST, load(MapSettings("color_palette" to "UNKNOWN")))
  }

  @Test
  fun `saved DYNAMIC is read as DYNAMIC`() {
    assertEquals(ColorPalette.DYNAMIC, load(MapSettings("color_palette" to "DYNAMIC")))
  }

  @Test
  fun `migration is idempotent and removes the old key`() {
    val settings = MapSettings("use_dynamic_colors" to true)
    assertEquals(ColorPalette.DYNAMIC, load(settings))
    assertFalse(settings.hasKey("use_dynamic_colors"))
    assertEquals(ColorPalette.DYNAMIC, load(settings))
  }

  @Test
  fun `chosen palette is persisted`() {
    val settings = MapSettings()
    ThemePreferencesImpl(settings).setColorPalette(ColorPalette.CAMP)
    assertEquals(ColorPalette.CAMP, load(settings))
  }
}
