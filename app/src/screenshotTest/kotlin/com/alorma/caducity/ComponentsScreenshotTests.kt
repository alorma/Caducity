package com.alorma.caducity

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.components.StatusBadgePreview
import com.android.tools.screenshot.PreviewTest

class ComponentsScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = 411, heightDp = 300)
  @Composable
  fun statusBadge() {
    StatusBadgePreview()
  }
}
