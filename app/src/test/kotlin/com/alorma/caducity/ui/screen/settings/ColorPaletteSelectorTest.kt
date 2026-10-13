package com.alorma.caducity.ui.screen.settings

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.alorma.caducity.ui.screen.settings.components.ColorPaletteSelector
import com.alorma.caducity.ui.theme.AppThemeContent
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferencesImpl
import com.alorma.caducity.ui.theme.colors.tokens
import com.russhwolf.settings.MapSettings
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ColorPaletteSelectorTest {
  @get:Rule
  val rule = createComposeRule()

  private val palettes = ColorPalette.entries.filter { it != ColorPalette.DYNAMIC }

  private fun setContent(
    prefs: ThemePreferencesImpl,
    onTheme: (androidx.compose.ui.graphics.Color) -> Unit = {},
  ) {
    prefs.setThemeModeState(ThemeMode.LIGHT)
    rule.setContent {
      AppThemeContent(prefs) {
        onTheme(CaducityTheme.colorScheme.primary)
        ColorPaletteSelector(
          selected = prefs.colorPalette.value,
          palettes = palettes,
          onSelected = { prefs.setColorPalette(it) },
        )
      }
    }
  }

  @Test
  fun `exactly one swatch is selected and each one is a named radio button`() {
    setContent(ThemePreferencesImpl(MapSettings()))
    rule.onNodeWithContentDescription("Pantry").assertIsSelected()
    val radios = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
    rule.onAllNodes(radios).assertCountEquals(palettes.size)
    rule
      .onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
      .assertCountEquals(1)
  }

  @Test
  fun `tapping a palette stores it and repaints the theme`() {
    val prefs = ThemePreferencesImpl(MapSettings())
    var primary = androidx.compose.ui.graphics.Color.Unspecified
    setContent(prefs) { primary = it }
    rule.onNodeWithContentDescription("Fridge").performClick()
    rule.waitForIdle()
    rule.mainClock.advanceTimeBy(1_000)
    assertEquals(ColorPalette.NEVERA, prefs.colorPalette.value)
    rule.onNodeWithContentDescription("Fridge").assertIsSelected()
    assertEquals(ColorPalette.NEVERA.tokens(false).primary, primary)
  }
}
