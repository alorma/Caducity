package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.screen.home.HomeEmptyPreviewContent
import com.alorma.caducity.ui.screen.home.HomeNothingSoonPreviewContent
import com.alorma.caducity.ui.screen.home.HomePreviewContent
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.android.tools.screenshot.PreviewTest

private const val Width = 411
private const val Height = 1100
private const val Night = Configuration.UI_MODE_NIGHT_YES

class HomeScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun homeSoon() {
    PreviewTheme { HomePreviewContent() }
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun homeNothingSoon() {
    PreviewTheme { HomeNothingSoonPreviewContent() }
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun homeEmpty() {
    PreviewTheme { HomeEmptyPreviewContent() }
  }
}
