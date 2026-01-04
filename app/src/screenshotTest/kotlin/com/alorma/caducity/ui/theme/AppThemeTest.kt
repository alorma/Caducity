package com.alorma.caducity.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class AppThemeTest {

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun MaterialColorsPreviewTheme() {
    MaterialColorsScreenshot()
  }

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun ExpirationColorsVibrantPreviewTheme() {
    ExpirationColorsVibrantScreenshot()
  }

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun ExpirationColorsSoftPreviewTheme() {
    ExpirationColorsSoftScreenshot()
  }

}