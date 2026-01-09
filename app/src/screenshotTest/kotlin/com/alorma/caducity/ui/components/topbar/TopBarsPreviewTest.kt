package com.alorma.caducity.ui.components.topbar

import androidx.compose.runtime.Composable
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class TopBarsPreviewTest {

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun StyledTopBarTest() {
    StyledTopBarScreenshot()
  }
}