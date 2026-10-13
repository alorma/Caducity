package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.alorma.caducity.ui.screen.home.HomePreviewContent
import com.alorma.caducity.ui.screen.pantry.PantryPreviewContent
import com.alorma.caducity.ui.screen.settings.SettingsPreviewContent
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.android.tools.screenshot.PreviewTest

class PalettesProvider : PreviewParameterProvider<ColorPalette> {
  override val values = ColorPalette.entries.filter { it != ColorPalette.DYNAMIC }.asSequence()
}

/** Inici, Rebost i Ajustos en les 6 paletes, en clar i fosc. */
class PalettesScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = 411, heightDp = 900)
  @Preview(name = "Dark", locale = "en", widthDp = 411, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun home(
    @PreviewParameter(PalettesProvider::class) palette: ColorPalette,
  ) {
    PreviewTheme(palette, darkTheme = isNight()) { HomePreviewContent() }
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = 411, heightDp = 900)
  @Preview(name = "Dark", locale = "en", widthDp = 411, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun pantry(
    @PreviewParameter(PalettesProvider::class) palette: ColorPalette,
  ) {
    PreviewTheme(palette, darkTheme = isNight()) { PantryPreviewContent() }
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = 411, heightDp = 1100)
  @Preview(name = "Dark", locale = "en", widthDp = 411, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun settings(
    @PreviewParameter(PalettesProvider::class) palette: ColorPalette,
  ) {
    PreviewTheme(palette, darkTheme = isNight()) { SettingsPreviewContent(palette) }
  }
}

@Composable
private fun isNight(): Boolean =
  (androidx.compose.ui.platform.LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
    Configuration.UI_MODE_NIGHT_YES
