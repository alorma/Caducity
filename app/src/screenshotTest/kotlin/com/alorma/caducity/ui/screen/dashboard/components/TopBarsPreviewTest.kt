package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.runtime.Composable
import com.alorma.caducity.ui.components.StyledCenterAlignedTopBarScreenshot
import com.alorma.caducity.ui.components.StyledTopBarScreenshot
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class TopBarsPreviewTest {
  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun StyledTopBarTest() {
    StyledCenterAlignedTopBarScreenshot()
  }

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun StyledCenterAlignedTest() {
    StyledTopBarScreenshot()
  }
}