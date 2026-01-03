package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.components.StyledCenterAlignedTopBarPreviewTheme
import com.alorma.caducity.ui.components.StyledTopBarPreviewTheme
import com.android.tools.screenshot.PreviewTest

class TopBarsPreviewTest {
  @PreviewTest
  @Preview
  @Composable
  fun StyledTopBarTest() {
    StyledTopBarPreviewTheme()
  }

  @PreviewTest
  @Preview
  @Composable
  fun StyledCenterAlignedTest() {
    StyledCenterAlignedTopBarPreviewTheme()
  }
}