package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.navigation.FloatingNavBarHomePreview
import com.alorma.caducity.ui.navigation.FloatingNavBarPantryPreview
import com.android.tools.screenshot.PreviewTest

private const val Width = 411
private const val Height = 100
private const val Night = Configuration.UI_MODE_NIGHT_YES

class NavigationScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun navBarHome() {
    FloatingNavBarHomePreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "ca", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun navBarPantry() {
    FloatingNavBarPantryPreview()
  }

  @PreviewTest
  @Preview(name = "Font 200", locale = "ca", widthDp = Width, heightDp = Height + 40, fontScale = 2f)
  @Composable
  fun navBarHomeLargeFont() {
    FloatingNavBarHomePreview()
  }
}
