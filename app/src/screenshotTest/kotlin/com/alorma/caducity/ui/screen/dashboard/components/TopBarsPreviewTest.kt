package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.components.StyledCenterAlignedTopBarPreviewTheme
import com.alorma.caducity.ui.components.StyledTopBarPreviewTheme
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class TopBarsPreviewTest {
  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun StyledTopBarTest() {
    StyledTopBarPreviewTheme()
  }

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun StyledCenterAlignedTest() {
    StyledCenterAlignedTopBarPreviewTheme()
  }
}