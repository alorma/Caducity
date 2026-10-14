package com.alorma.caducity.ui.components.actions

import android.app.Application
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.TouchApp
import com.alorma.caducity.ui.theme.AppThemeContent
import com.alorma.caducity.ui.theme.ThemePreferencesImpl
import com.russhwolf.settings.MapSettings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HoldToConfirmRowTest {
  @get:Rule
  val rule = createComposeRule()

  private var confirmed = 0

  @Before
  fun setUp() {
    confirmed = 0
    rule.mainClock.autoAdvance = false
    rule.setContent {
      AppThemeContent(ThemePreferencesImpl(MapSettings())) {
        HoldToConfirmRow(
          icon = Symbols.Outlined.TouchApp,
          title = "Eat it anyway",
          subtitle = "Press and hold",
          accessibilityLabel = "Eat it anyway",
          onConfirmed = { confirmed++ },
          onRequestConfirm = {},
          shape = RoundedCornerShape(8.dp),
        )
      }
    }
  }

  @Test
  fun `holding for the whole time confirms`() {
    rule.onNodeWithText("Eat it anyway").performTouchInput { down(center) }
    rule.mainClock.advanceTimeBy(1_400)
    assertEquals(0, confirmed)

    rule.mainClock.advanceTimeBy(200)
    assertEquals(1, confirmed)
    rule.onNodeWithText("Eat it anyway").performTouchInput { up() }
  }

  @Test
  fun `letting go before the time is up does not confirm`() {
    rule.onNodeWithText("Eat it anyway").performTouchInput { down(center) }
    rule.mainClock.advanceTimeBy(1_000)
    rule.onNodeWithText("Eat it anyway").performTouchInput { up() }

    rule.mainClock.advanceTimeBy(2_000)
    assertEquals(0, confirmed)
  }
}
